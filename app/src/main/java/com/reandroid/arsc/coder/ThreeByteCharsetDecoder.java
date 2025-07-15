/*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.arsc.coder;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

public class ThreeByteCharsetDecoder extends CharsetDecoder {
    public static final ThreeByteCharsetDecoder INSTANCE = new ThreeByteCharsetDecoder();
    public ThreeByteCharsetDecoder() {
        super(StandardCharsets.UTF_8, 1.0F, 1.0F);
    }
    @Override
    protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        return src.hasArray() && dst.hasArray() ? this.decodeArrayLoop(src, dst) : this.decodeBufferLoop(src, dst);
    }
    private CoderResult decodeArrayLoop(ByteBuffer src, CharBuffer dst) {
        byte[] srcBytes = src.array();
        int sourcePosition = src.arrayOffset() + src.position();
        int sourceLimit = src.arrayOffset() + src.limit();
        char[] dstChars = dst.array();
        int dstPosition = dst.arrayOffset() + dst.position();
        int dstLimit = dst.arrayOffset() + dst.limit();
        int min = sourceLimit - sourcePosition;
        int start = min;
        min = dstLimit - dstPosition;
        if(min < start){
            start = min;
        }
        start = dstPosition + start;

        while ( dstPosition < start && srcBytes[sourcePosition] >= 0) {
            dstChars[dstPosition++] = (char) srcBytes[sourcePosition++];
        }

        while (sourcePosition < sourceLimit) {
            int b1 = srcBytes[sourcePosition];
            if (b1 < 0) {
                if (b1 >> 5 == -2 && (b1 & 0x1E) != 0) {
                    if (sourceLimit - sourcePosition < 2 || dstPosition >= dstLimit) {
                        return xFlow(src, sourcePosition, sourceLimit, dst, dstPosition, 2);
                    }
                    int b2 = srcBytes[sourcePosition + 1];
                    if (isNotContinuation(b2)) {
                        return malformedForLength(src, sourcePosition, dst, dstPosition);
                    }
                    dstChars[dstPosition++] = (char) (b1 << 6 ^ b2 ^ 0x0F80);
                    sourcePosition += 2;
                } else {
                    if (b1 >> 4 != -2) {
                        return malformed(src, sourcePosition, dst, dstPosition, 1);
                    }

                    int srcRemaining = sourceLimit - sourcePosition;
                    if (srcRemaining < 3 || dstPosition >= dstLimit) {
                        if (srcRemaining > 1 && isMalformed3_2(b1, srcBytes[sourcePosition + 1])) {
                            return malformedForLength(src, sourcePosition, dst, dstPosition);
                        }

                        return xFlow(src, sourcePosition, sourceLimit, dst, dstPosition, 3);
                    }

                    int b2 = srcBytes[sourcePosition + 1];
                    int b3 = srcBytes[sourcePosition + 2];
                    if (isMalformed3(b1, b2, b3)) {
                        return malformed(src, sourcePosition, dst, dstPosition, 3);
                    }

                    dstChars[dstPosition++] = (char) (b1 << 12 ^ b2 << 6 ^ b3 ^ 0xFFFE1F80);
                    sourcePosition += 3;
                }
            } else {
                if (dstPosition >= dstLimit) {
                    return xFlow(src, sourcePosition, sourceLimit, dst, dstPosition, 1);
                }

                dstChars[dstPosition++] = (char) b1;
                ++sourcePosition;
            }
        }

        return xFlow(src, sourcePosition, sourceLimit, dst, dstPosition, 0);
    }
    private CoderResult decodeBufferLoop(ByteBuffer src, CharBuffer dst) {
        int mark = src.position();
        int limit = src.limit();

        while (mark < limit) {
            int b1 = src.get();
            if (b1 < 0) {
                if (b1 >> 5 == -2 && (b1 & 0x1E) != 0) {
                    if (limit - mark < 2 || dst.remaining() < 1) {
                        return xFlow(src, mark, 2);
                    }
                    int b2 = src.get();
                    if (isNotContinuation(b2)) {
                        return malformedForLength(src, mark);
                    }
                    dst.put((char) (b1 << 6 ^ b2 ^ 0x0F80));
                    mark += 2;
                } else {
                    if (b1 >> 4 != -2) {
                        return malformed(src, mark, 1);
                    }

                    int srcRemaining = limit - mark;
                    if (srcRemaining < 3 || dst.remaining() < 1) {
                        if (srcRemaining > 1 && isMalformed3_2(b1, src.get())) {
                            return malformedForLength(src, mark);
                        }

                        return xFlow(src, mark, 3);
                    }

                    int b2 = src.get();
                    int b3 = src.get();
                    if (isMalformed3(b1, b2, b3)) {
                        return malformed(src, mark, 3);
                    }

                    dst.put((char) (b1 << 12 ^ b2 << 6 ^ b3 ^ 0xFFFE1F80));
                    mark += 3;
                }
            } else {
                if (dst.remaining() < 1) {
                    return xFlow(src, mark, 1);
                }

                dst.put((char) b1);
                ++mark;
            }
        }
        return xFlow(src, mark, 0);
    }

    private static void updatePositions(ByteBuffer src, int sourcePosition, CharBuffer dst, int dstPosition) {
        src.position(sourcePosition - src.arrayOffset());
        dst.position(dstPosition - dst.arrayOffset());
    }
    private static boolean isNotContinuation(int b) {
        return (b & 0xC0) != 0x80;
    }
    private static boolean isMalformed3(int b1, int b2, int b3) {
        return b1 == -32 && (b2 & 0xE0) == 0x80 || (b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80;
    }
    private static boolean isMalformed3_2(int b1, int b2) {
        return b1 == -32 && (b2 & 0xE0) == 0x80 || (b2 & 0xC0) != 0x80;
    }
    private static CoderResult malformedN(ByteBuffer src, int nb) {
        int b1;
        int b2;
        switch (nb) {
            case 1:
            case 2:
                return CoderResult.malformedForLength(1);
            case 3:
                b1 = src.get();
                b2 = src.get();
                return CoderResult.malformedForLength((b1 != -32 || (b2 & 0xE0) != 0x80) && !isNotContinuation(b2) ? 2 : 1);
            case 4:
                b1 = src.get() & 0xFF;
                b2 = src.get() & 0xFF;
                if (b1 <= 244
                        && (b1 != 0xF0 || b2 >= 144 && b2 <= 0xBF)
                        && (b1 != 244 || (b2 & 0xF0) == 0x80)
                        && !isNotContinuation(b2)) {
                    if (isNotContinuation(src.get())) {
                        return CoderResult.malformedForLength(2);
                    }
                    return CoderResult.malformedForLength(3);
                }
                return CoderResult.malformedForLength(1);
            default:
                return null;
        }
    }
    private static CoderResult malformed(ByteBuffer src, int sourcePosition, CharBuffer dst, int dstPosition, int numBytes) {
        src.position(sourcePosition - src.arrayOffset());
        CoderResult cr = malformedN(src, numBytes);
        updatePositions(src, sourcePosition, dst, dstPosition);
        return cr;
    }
    private static CoderResult malformed(ByteBuffer src, int mark, int nb) {
        src.position(mark);
        CoderResult cr = malformedN(src, nb);
        src.position(mark);
        return cr;
    }
    private static CoderResult malformedForLength(ByteBuffer src, int sourcePosition, CharBuffer dst, int dstPosition) {
        updatePositions(src, sourcePosition, dst, dstPosition);
        return CoderResult.malformedForLength(1);
    }
    private static CoderResult malformedForLength(ByteBuffer src, int mark) {
        src.position(mark);
        return CoderResult.malformedForLength(1);
    }
    private static CoderResult xFlow(ByteBuffer src, int sourcePosition, int sourceLimit, CharBuffer dst, int dstPosition, int numBytes) {
        updatePositions(src, sourcePosition, dst, dstPosition);
        return numBytes != 0 && sourceLimit - sourcePosition >= numBytes ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
    }
    private static CoderResult xFlow(Buffer src, int mark, int nb) {
        src.position(mark);
        return nb != 0 && src.remaining() >= nb ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
    }
}