/*
 * This file is part of MOS
 * <p>
 * Copyright (c) 2021 by cooder.org
 * <p>
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.cooder.mos;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {

    public static void close(Closeable res) {
        try {
            if (res != null) {
                res.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    public static String[] parseArgs(String text) {
        char[] src = text.toCharArray();

        List<String> list = new ArrayList<String>();

        StringBuilder sb = new StringBuilder();
        boolean quote = false, slash = false;
        for (int i = 0; i < src.length; i++) {
            char c = src[i];
            if (slash) {
                if (c == 'n') {
                    sb.append('\n');
                } else if (c == 't') {
                    sb.append('\t');
                } else {
                    sb.append(c);
                }
                slash = false;
                continue;
            } else if (c == '\\') {
                slash = true;
                continue;
            } else if (quote) {
                if (c != '"') {
                    sb.append(c);
                } else {
                    list.add(sb.toString());
                    sb = new StringBuilder();
                    quote = false;
                }
                continue;
            } else if (c == '"') {
                if (sb.length() == 0) {
                    quote = true;
                    continue;
                }
            }

            if (c == '>') {
                if (sb.length() != 0) {
                    list.add(sb.toString());
                    sb = new StringBuilder();
                }

                if (i + 1 < src.length && src[i + 1] == '>') {
                    list.add(">>");
                    i = i + 1;
                } else {
                    list.add(">");
                }
                continue;
            }

            if (!Character.isSpaceChar(c)) {
                sb.append(c);
            } else {
                if (sb.length() > 0) {
                    list.add(sb.toString());
                    sb = new StringBuilder();
                }
            }
        }

        if (sb.length() > 0) {
            list.add(sb.toString());
        }

        return list.toArray(new String[0]);
    }

    public static void copyStreamNoCloseOut(InputStream is, OutputStream out) throws IOException {
        try {
            int v = 0;
            while ((v = is.read()) != -1) {
                out.write(v);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            out.flush();
            close(is);
        }
    }

    public static String time2String(long mills) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(new Date(mills));
    }
}
