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
    // log出力時、改行が2行入り一行空行ができてしまうので改行を1つ除く
    if (string.endsWith("\n")) {
      string = string.substring(0, string.length() - 1);
    }
    
    detailLog.debug(string);
  }
}
