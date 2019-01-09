import java.util.List;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.*;
import java.nio.file.*;
import java.util.Properties;
import java.io.FileReader;

/**
 * Tool to insert Blacklist into database. 
 * 
 * Fill out insert.properties with the values appropriate to the database used. Make sure batching is enabled.
 * See https://www.ibm.com/support/knowledgecenter/en/SSEP7J_10.1.1/com.ibm.swg.ba.cognos.vvm_ag_guide.10.1.1.doc/c_ag_samjdcurlform.html
 * for some JDBC URL examples. Or try http://lmgtfy.com/?q=jdbc+url+examples
 *
 * Compile the program (requires Java >= 8):
 *     javac -cp . BlacklistInsert.java
 * 
 * Run the program:
 *     java -cp "$HOME/.m2/repository/mysql/mysql-connector-java/5.1.28/mysql-connector-java-5.1.28.jar:." BlacklistInsert insert.properties
 *
 */
public class BlacklistInsert {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        
        try (FileReader fileReader = new FileReader(args[0]);) {
            properties.load(fileReader);
            
            List<Path> paths = Files.walk(Paths.get(properties.getProperty("path")))
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith("domains"))
                .collect(Collectors.toList());
            try {
                for (Path p : paths) {
                    System.out.println(String.format("%s %d - %s", p.toString(), Files.lines(p).count(), p.subpath(1, 2)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (Connection con = DriverManager.getConnection(properties.getProperty("jdbc.url"), properties.getProperty("db.user"), properties.getProperty("db.password"));) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO domain_entry (`domain`, `category`) VALUES (?, ?)")) {
                    for (Path p : paths) {
                        String category = p.subpath(1, 2).toString();
                        Files.lines(p).forEach(s -> {
                                try {
                                    ps.setString(1, s);
                                    ps.setString(2, category);
                                    ps.addBatch();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                    }
                    ps.executeBatch();
                }
            }
        }
    }
}
