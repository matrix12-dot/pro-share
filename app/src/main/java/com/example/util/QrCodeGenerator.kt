package com.example.util

/**
 * Lightweight pure-Kotlin QR Code Generator (Model 2, Version 2-4, Byte Mode)
 * Capable of encoding URLs like "http://192.168.1.100:8080" directly into a boolean matrix
 * with zero external dependencies.
 */
object QrCodeGenerator {

    /**
     * Generates a 2D boolean array representing the QR code matrix (true = dark module, false = light module).
     */
    fun encode(text: String): Array<BooleanArray> {
        val bytes = text.toByteArray(Charsets.ISO_8859_1)
        val len = bytes.size

        // Version selection based on data length (using EC level L for maximum data capacity)
        val version = when {
            len <= 17 -> 1 // 21x21
            len <= 32 -> 2 // 25x25
            len <= 53 -> 3 // 29x29
            len <= 78 -> 4 // 33x33
            else -> 5      // 37x37
        }

        val size = version * 4 + 17
        val matrix = Array(size) { BooleanArray(size) }
        val reserved = Array(size) { BooleanArray(size) }

        // 1. Finder patterns (top-left, top-right, bottom-left)
        addFinderPattern(matrix, reserved, 0, 0)
        addFinderPattern(matrix, reserved, size - 7, 0)
        addFinderPattern(matrix, reserved, 0, size - 7)

        // 2. Alignment patterns for version >= 2
        if (version >= 2) {
            val alignPos = when (version) {
                2 -> intArrayOf(6, 18)
                3 -> intArrayOf(6, 22)
                4 -> intArrayOf(6, 26)
                else -> intArrayOf(6, 30)
            }
            for (r in alignPos) {
                for (c in alignPos) {
                    if (!reserved[r][c]) {
                        addAlignmentPattern(matrix, reserved, r - 2, c - 2)
                    }
                }
            }
        }

        // 3. Timing patterns
        for (i in 8 until size - 8) {
            val dark = (i % 2 == 0)
            if (!reserved[6][i]) {
                matrix[6][i] = dark
                reserved[6][i] = true
            }
            if (!reserved[i][6]) {
                matrix[i][6] = dark
                reserved[i][6] = true
            }
        }

        // 4. Dark module
        matrix[4 * version + 9][8] = true
        reserved[4 * version + 9][8] = true

        // 5. Reserve format info areas
        for (i in 0..8) {
            reserved[8][i] = true
            reserved[i][8] = true
            if (size - 1 - i < size) {
                reserved[8][size - 1 - i] = true
                reserved[size - 1 - i][8] = true
            }
        }

        // 6. Data payload construction (Byte mode: indicator 0100 + char count + data + terminator + pad)
        val bitBuffer = mutableListOf<Boolean>()
        // Mode: Byte (0100)
        appendBits(bitBuffer, 4, 4)
        // Character count indicator (8 bits for versions 1-9)
        appendBits(bitBuffer, len, 8)
        // Data bytes
        for (b in bytes) {
            appendBits(bitBuffer, b.toInt() and 0xFF, 8)
        }
        // Terminator (up to 4 zeroes)
        val totalCapacityBits = getTotalDataCodewords(version) * 8
        val termLen = (totalCapacityBits - bitBuffer.size).coerceIn(0, 4)
        appendBits(bitBuffer, 0, termLen)
        // Pad to byte boundary
        while (bitBuffer.size % 8 != 0) {
            bitBuffer.add(false)
        }
        // Pad bytes (0xEC, 0x11 alternating)
        val padBytes = intArrayOf(0xEC, 0x11)
        var padIdx = 0
        while (bitBuffer.size < totalCapacityBits) {
            appendBits(bitBuffer, padBytes[padIdx % 2], 8)
            padIdx++
        }

        // Convert bits to codewords
        val dataCodewords = IntArray(bitBuffer.size / 8)
        for (i in dataCodewords.indices) {
            var byteVal = 0
            for (bit in 0 until 8) {
                if (bitBuffer[i * 8 + bit]) {
                    byteVal = byteVal or (1 shl (7 - bit))
                }
            }
            dataCodewords[i] = byteVal
        }

        // Error correction codewords using Reed-Solomon
        val ecCount = getEcCodewordsCount(version)
        val allCodewords = generateCodewordsWithEc(dataCodewords, ecCount)

        // 7. Place data into matrix with masking (mask 0: (row + col) % 2 == 0)
        placeData(matrix, reserved, allCodewords, size)

        // 8. Format Information (EC Level L, Mask 0: 01 000 -> 0x77C4 with BCH 15,5)
        applyFormatInfo(matrix, size, 0x77C4)

        return matrix
    }

    private fun appendBits(buffer: MutableList<Boolean>, value: Int, length: Int) {
        for (i in length - 1 downTo 0) {
            buffer.add(((value shr i) and 1) == 1)
        }
    }

    private fun addFinderPattern(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, row: Int, col: Int) {
        for (r in -1..7) {
            for (c in -1..7) {
                val pr = row + r
                val pc = col + c
                if (pr in matrix.indices && pc in matrix.indices) {
                    val isDark = (r in 0..6 && (c == 0 || c == 6)) ||
                            (c in 0..6 && (r == 0 || r == 6)) ||
                            (r in 2..4 && c in 2..4)
                    matrix[pr][pc] = isDark
                    reserved[pr][pc] = true
                }
            }
        }
    }

    private fun addAlignmentPattern(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, row: Int, col: Int) {
        for (r in 0..4) {
            for (c in 0..4) {
                val pr = row + r
                val pc = col + c
                val isDark = r == 0 || r == 4 || c == 0 || c == 4 || (r == 2 && c == 2)
                matrix[pr][pc] = isDark
                reserved[pr][pc] = true
            }
        }
    }

    private fun getTotalDataCodewords(version: Int): Int = when (version) {
        1 -> 19
        2 -> 34
        3 -> 55
        4 -> 80
        else -> 108
    }

    private fun getEcCodewordsCount(version: Int): Int = when (version) {
        1 -> 7
        2 -> 10
        3 -> 15
        4 -> 20
        else -> 26
    }

    // Reed-Solomon encoder for GF(2^8) with polynomial 0x11D
    private fun generateCodewordsWithEc(data: IntArray, ecCount: Int): IntArray {
        val exp = IntArray(512)
        val log = IntArray(256)
        var x = 1
        for (i in 0 until 255) {
            exp[i] = x
            exp[i + 255] = x
            log[x] = i
            x = (x shl 1) xor if (x >= 128) 0x11D else 0
        }

        // Generator polynomial for ecCount
        var gen = intArrayOf(1)
        for (i in 0 until ecCount) {
            val nextGen = IntArray(gen.size + 1)
            for (j in gen.indices) {
                nextGen[j] = nextGen[j] xor gen[j]
                val root = exp[i]
                val prod = if (gen[j] == 0) 0 else exp[log[gen[j]] + log[root]]
                nextGen[j + 1] = nextGen[j + 1] xor prod
            }
            gen = nextGen
        }

        val result = IntArray(data.size + ecCount)
        System.arraycopy(data, 0, result, 0, data.size)

        for (i in data.indices) {
            val coef = result[i]
            if (coef != 0) {
                val logCoef = log[coef]
                for (j in 1 until gen.size) {
                    val prod = exp[logCoef + log[gen[j]]]
                    result[i + j] = result[i + j] xor prod
                }
            }
        }

        System.arraycopy(data, 0, result, 0, data.size)
        return result
    }

    private fun placeData(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, codewords: IntArray, size: Int) {
        val bits = BooleanArray(codewords.size * 8)
        for (i in codewords.indices) {
            val cw = codewords[i]
            for (b in 0 until 8) {
                bits[i * 8 + b] = ((cw shr (7 - b)) and 1) == 1
            }
        }

        var bitIndex = 0
        var col = size - 1
        var upwards = true

        while (col > 0) {
            if (col == 6) col-- // Skip vertical timing pattern

            val rowRange = if (upwards) (size - 1 downTo 0) else (0 until size)
            for (row in rowRange) {
                for (c in 0..1) {
                    val currentCol = col - c
                    if (!reserved[row][currentCol]) {
                        var bit = false
                        if (bitIndex < bits.size) {
                            bit = bits[bitIndex++]
                        }
                        // Mask 0: (row + col) % 2 == 0
                        val mask = ((row + currentCol) % 2 == 0)
                        matrix[row][currentCol] = bit xor mask
                    }
                }
            }
            upwards = !upwards
            col -= 2
        }
    }

    private fun applyFormatInfo(matrix: Array<BooleanArray>, size: Int, formatBits: Int) {
        for (i in 0 until 15) {
            val bit = ((formatBits shr (14 - i)) and 1) == 1
            // Top-left
            when {
                i <= 5 -> matrix[i][8] = bit
                i == 6 -> matrix[7][8] = bit
                i == 7 -> matrix[8][8] = bit
                i == 8 -> matrix[8][7] = bit
                else -> matrix[8][14 - i] = bit
            }
            // Bottom-left / Top-right
            if (i < 8) {
                matrix[8][size - 1 - i] = bit
            } else {
                matrix[size - 15 + i][8] = bit
            }
        }
    }
}
