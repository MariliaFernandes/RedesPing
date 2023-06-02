import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class VerificacaoConectividade {

    public static void main(String[] args) {
        String enderecoServidor = obterEnderecoServidor();
        boolean conectividade = verificarConectividade(enderecoServidor);
        exibirResultado(conectividade);
        salvarInformacoesNoBanco(enderecoServidor, conectividade);
        exibirRegistrosDoBanco();
    }

    private static String obterEnderecoServidor() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o endereço IP ou nome de domínio do servidor remoto: ");
        return scanner.nextLine();
    }

    private static boolean verificarConectividade(String enderecoServidor) {
        try {
            InetAddress servidor = InetAddress.getByName(enderecoServidor);
            if (servidor.isReachable(1000)) {
                return true;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void exibirResultado(boolean conectividade) {
        if (conectividade) {
            System.out.println("Conectividade estabelecida. O servidor respondeu ao ping.");
        } else {
            System.out.println("Conectividade falhou. O servidor não respondeu ao ping.");
        }
    }

    private static void salvarInformacoesNoBanco(String enderecoServidor, boolean conectividade) {
        Connection connection = null;
        try {
            // Conecta ao banco de dados SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:conectividade.db");

            // Cria a tabela caso não exista
            String createTableQuery = "CREATE TABLE IF NOT EXISTS conectividade (endereco_servidor TEXT, conectividade INTEGER)";
            connection.prepareStatement(createTableQuery).execute();

            // Insere as informações na tabela
            String insertQuery = "INSERT INTO conectividade (endereco_servidor, conectividade) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, enderecoServidor);
            preparedStatement.setBoolean(2, conectividade);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void exibirRegistrosDoBanco() {
        Connection connection = null;
        try {
            // Conecta ao banco de dados SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:conectividade.db");

            // Executa a consulta para obter os registros
            String selectQuery = "SELECT endereco_servidor, conectividade FROM conectividade";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Exibe os registros
            while (resultSet.next()) {
                String enderecoServidor = resultSet.getString("endereco_servidor");
                boolean conectividade = resultSet.getBoolean("conectividade");
                System.out.println("Endereço do servidor: " + enderecoServidor);
                System.out.println("Conectividade: " + (conectividade ? "Estabelecida" : "Falhou"));
                System.out.println("-----------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
