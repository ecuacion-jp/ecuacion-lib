/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ecuacion.lib.core.util.internal;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import jp.ecuacion.lib.core.logging.DetailLogger;

/**
 * @see <a href="https://stackoverflow.com/questions/2118370/how-to-redirect-javax-mail-session-setdebugout-to-log4j-logger">stackOverflow</a>
 */
public class MailUtilLogOutputStream extends FilterOutputStream {

  private static DetailLogger detailLog = new DetailLogger(MailUtilLogOutputStream.class);
  private static final ByteArrayOutputStream bos = new ByteArrayOutputStream();

  public MailUtilLogOutputStream() {
    super(bos);
  }

  @Override
  public void flush() throws IOException {
    // this was never called in my test
    bos.flush();
    if (bos.size() > 0) {
      detailLog.info(bos.toString());
    }

    bos.reset();
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[] {(byte) b});
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(new String(b));
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    write(new String(b, off, len));
  }
  
  private void write(String string) {
    // Remove one newline because one empty line created by two newlines on log output.
    if (string.endsWith("\n")) {
      string = string.substring(0, string.length() - 1);
    }
    
    detailLog.debug(string);
  }
}
