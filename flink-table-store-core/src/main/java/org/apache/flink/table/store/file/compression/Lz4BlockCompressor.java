/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.store.file.compression;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import static org.apache.flink.table.store.file.compression.CompressorUtils.HEADER_LENGTH;
import static org.apache.flink.table.store.file.compression.CompressorUtils.writeIntLE;

/**
 * Encode data into LZ4 format (not compatible with the LZ4 Frame format). It reads from and writes
 * to byte arrays provided from the outside, thus reducing copy time.
 *
 * <p>This class is copied and modified from {@link net.jpountz.lz4.LZ4BlockOutputStream}.
 */
public class Lz4BlockCompressor implements BlockCompressor {

    private final LZ4Compressor compressor;

    public Lz4BlockCompressor() {
        this.compressor = LZ4Factory.fastestInstance().fastCompressor();
    }

    @Override
    public int getMaxCompressedSize(int srcSize) {
        return HEADER_LENGTH + compressor.maxCompressedLength(srcSize);
    }

    @Override
    public int compress(byte[] src, int srcOff, int srcLen, byte[] dst, int dstOff)
            throws BufferCompressionException {
        try {
            int compressedLength =
                    compressor.compress(src, srcOff, srcLen, dst, dstOff + HEADER_LENGTH);
            writeIntLE(compressedLength, dst, dstOff);
            writeIntLE(srcLen, dst, dstOff + 4);
            return HEADER_LENGTH + compressedLength;
        } catch (Exception e) {
            throw new BufferCompressionException(e);
        }
    }
}
