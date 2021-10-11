package ru.falsher.base64;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Base64DecodeInputStream extends InputStream {

    private int buffer = 0;
    private int bufferLen = 0;

    private int[] buf = new int[8];
    private int bufLen = 0;
    private int bufIndex = 0;

    private final InputStream in;

    public Base64DecodeInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        if (bufLen != 0 && bufLen == bufIndex) bufLen = bufIndex = 0;
        while (true) {
            int b;
            if (bufferLen == 0) b = 0;
            else b = buffer << (8 - bufferLen);
            int i = in.read();
            while (i == '=') {
                if (bufLen - bufIndex != 0) bufLen--;
                else return -1;
                i = in.read();
            }
            if (i == -1) {
                if (bufLen == bufIndex) {
                    if (bufferLen != 0) {
                        bufferLen = 0;
                        return b;
                    } else return -1;
                } else return buf[bufIndex++];
            }
            i = getIndexFromChar(i);
            if (bufferLen == 0) {
                buffer = i;
                bufferLen = 6;
                continue;
            } else {
                bufferLen -= 2;
                b |= (i >> bufferLen);
                buffer = i & ~(0xFF << bufferLen);
            }
            if (bufLen != 0) {
                if (buf.length == bufLen) buf = Arrays.copyOf(buf, buf.length + 8);
                buf[bufLen++] = b;
                if (b != 0) return buf[bufIndex++];
            } else return b;
        }
    }

    private int getIndexFromChar(int c){
        if (c < '+') return 0;
        else if (c == '+') return 62;
        else if (c < '/') return 0;
        else if (c == '/') return 63;
        else if (c <= '9') return (c - '0' + 52);
        else if (c < 'A') return 0;
        else if (c <= 'Z') return (c - 'A');
        else if (c < 'a') return 0;
        else if (c <= 'z') return (c - 'a' + 26);
        else return 0;
    }

    @Override
    public void close() throws IOException {
        in.close();
        buf = new int[0];
        bufIndex = bufLen = 0;
    }

}