package VaadinPortlet;

/**
 * Created with IntelliJ IDEA.
 * User: alexander.klimov
 * Date: 24.04.13
 * Time: 15:07
 * To change this template use File | Settings | File Templates.
 */
import java.net.URL;
import java.sql.*;
import java.io.*;
class SimpleSelect {
    public static void main (String args[]) {
        String url = "jdbc:mysql://localhost:3306/test";
        String query = "SELECT * FROM channels";
        try {
            // Загрузка jdbc-odbc-bridge драйвера
            Class.forName ("com.mysql.jdbc.Driver");
//            DriverManager.setLogStream(System.out);
            // Попытка соединения с драйвером. Каждый из
            // зарегистрированных драйверов будет загружаться, пока
            // не будет найден тот, который сможет обработать этот URL
            Connection con = DriverManager.getConnection (
                    url, "root", "unclefucka");
            // Если не можете соединиться, то произойдет exception
            // (исключительная ситуация). Однако, если вы попадете
            // в следующую строку программы, значит вы успешно соединились с URL
            // Проверки и печать сообщения об успешном соединении
            //
            checkForWarning (con.getWarnings ());
            // Получить DatabaseMetaData объект и показать
            // информацию о соединении
            DatabaseMetaData dma = con.getMetaData ();
            //System.out.println(_\nConnected to _ + dma.getURL());
            //System.out.println(_Driver _ +
            //dma.getDriverName());
//System.out.println(_Version _ +
            //dma.getDriverVersion());
            //System.out.println(__);
            // Создать Оператор-объект для посылки
            // SQL операторов в драйвер
            Statement stmt = con.createStatement ();
            // Образовать запрос, путем создания ResultSet объекта
            ResultSet rs = stmt.executeQuery (query);
            // Показать все колонки и ряды из набора результатов
            dispResultSet (rs);
            // Закрыть результирующий набор
            rs.close();
            // Закрыть оператор
            stmt.close();
            // Закрыть соединение
            con.close();
        }
        catch (SQLException ex) {
            // Случилось SQLException. Перехватим и
            // покажем информацию об ошибке. Заметим, что это
            // может быть множество ошибок, связанных вместе
            //
            //System.out.println (_\n*** SQLException caught ***\n_);
            while (ex != null) {
                //System.out.println (_SQLState: _ +
                // ex.getSQLState ());
                //System.out.println (_Message: _ + ex.getMessage ());
                //System.out.println (_Vendor: _ +
                //ex.getErrorCode ());
                ex = ex.getNextException ();
                //System.out.println (__);
            }
        }
        catch (java.lang.Exception ex) {
            // Получив некоторые другие типы exception, распечатаем их.
            ex.printStackTrace ();
        }
    }
    //----------------------------------
    // checkForWarning
    // Проверка и распечатка предупреждений. Возврат true если
    // предупреждение существует
    //----------------------------------
    private static boolean checkForWarning (SQLWarning warn)
            throws SQLException {
        boolean rc = false;
        // Если SQLWarning объект был получен, показать
        // предупреждающее сообщение.
        if (warn != null) {
            System.out.println ("\n *** Warning ***\n");
            rc = true;
            while (warn != null) {
                //System.out.println (_SQLState: _ +
                //warn.getSQLState ());
                //System.out.println (_Message: _ +
                //warn.getMessage ());
                //System.out.println (_Vendor: _ +
                //warn.getErrorCode ());
                //System.out.println (__);
                warn = warn.getNextWarning ();
            }
        }
        return rc;
    }
    //----------------------------------
    // dispResultSet
    // Показать таблицу полученных результатов
    //----------------------------------
    private static void dispResultSet (ResultSet rs)
            throws SQLException, IOException
    {
        // Объявление необходимых переменных и
        // константы для желаемой таблицы перекодировки данных
        int i, length, j;
        String cp1 = new String("Cp1251");
        // Получить the ResultSetMetaData. Они будут использованы
        // для печати заголовков
        ResultSetMetaData rsmd = rs.getMetaData ();
        // Получить номер столбца в результирующем наборе
        int numCols = rsmd.getColumnCount ();
        // Показать заголовок столбца
        for (i=1; i<=numCols; i++) {
            if (i > 1) System.out.print(",");
            //System.out.print(rsmd.getColumnLabel(i));
        }
        System.out.println("");
        // Показать данные, загружая их до тех пор, пока не исчерпается
        // результирующий набор
        boolean more = rs.next ();
        while (more) {
            // Цикл по столбцам
            for (i=1; i<=numCols; i++) {
// Следующая группа операторов реализует функции перекодировки
// строк из таблицы базы данных в желаемый формат, потому что в
// различных базах символы могут быть закодированы произвольным
// образом. Если использовать стандартный метод - getString - на выходе
// получается абракадабра. Строки нужно сначала перевести в Unicode,
// затем конвертировать в строку Windows и убрать лидирующие нули
                InputStream str1 = rs.getUnicodeStream(i);
                byte str2[];
                byte str3[];
                int sizeCol = rsmd.getColumnDisplaySize(i);
                str2 = new byte[sizeCol+sizeCol];
                str3 = new byte[sizeCol+sizeCol];
                length = str1.read(str2);
                // Здесь нужно убрать нули из строки, которые предваряют каждый
                // перекодированный символ
                int k=1;
                for (j=1; j<sizeCol*2; j++) {
                    if (str2[j] != 0) {
                        str3[k]=str2[j]; k=k+1; } }
                String str = new String(str3,cp1);
                System.out.print(str);
            }
            System.out.println("");
            // Загрузка следующего ряда в наборе
            more = rs.next ();
        }
    }
}