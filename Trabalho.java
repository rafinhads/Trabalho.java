import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

class Livro {
    public String titulo;
    public String autor;
    private Integer id;

    public Livro(Integer id, String titulo, String autor) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
    }

    public Integer getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Título: " + titulo + ", Autor: " + autor;
    }
}

class Conexaojdbc {

    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca";
    private static final String USUARIO = "root";
    private static final String SENHA = "12345678";

    public static Connection getConexao() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter a conexão: " + e.getMessage());
        }
    }
}

class Biblioteca {
    List<Livro> livros;
    Integer id;
    String nome;

    public Biblioteca() {
        try (Connection connection = Conexaojdbc.getConexao()) {
            String sql = "SELECT id, nome FROM biblioteca";
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {
                this.id = resultSet.getInt("id");
                this.nome = resultSet.getString("nome");
            }
            System.out.println(this.nome);
            this.livros = new ArrayList<>();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void adicionarLivro(String titulo, String autor) {
        try (Connection connection = Conexaojdbc.getConexao())  {
            String sql = " INSERT INTO livro (titulo, autor, biblioteca_id) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, titulo);
            statement.setString(2, autor);
            statement.setInt(3, this.id);
            statement.executeUpdate();
            System.out.println("Livro criado com sucesso no banco de dados.");
        } catch (Exception e) {
            System.out.println("Erro ao criar o livro: " + e.getMessage());
        }
    }

    public void removerLivro(Integer id) {
        try (Connection connection = Conexaojdbc.getConexao())  {
            String sql = "DELETE FROM livro WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("Livro removido com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao remover o livro: " + e.getMessage());
        }
    }

    public void alterarLivro(Integer id, String novoTitulo, String novoAutor) {
        try (Connection connection = Conexaojdbc.getConexao())  {
            String sql = "UPDATE livro SET titulo = ?, autor = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, novoTitulo);
            statement.setString(2, novoAutor);
            statement.setInt(3, id);
            statement.executeUpdate();
            System.out.println("Livro alterado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao alterar o livro: " + e.getMessage());
        }
    }

    public void listarLivros(boolean ordenarAscendente) {
        try (Connection connection = Conexaojdbc.getConexao())  {
            StringBuilder sql = new StringBuilder(" SELECT * FROM livro ");
            if (ordenarAscendente) {
                sql.append(" ORDER BY id ASC ");
            } else {
                sql.append(" ORDER BY id DESC ");
            }
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            ResultSet resultSet = statement.executeQuery();
            this.livros.clear();
            while (resultSet.next()) {
                Livro livro = new Livro(resultSet.getInt("id"), resultSet.getString("titulo"), resultSet.getString("autor"));
                this.livros.add(livro);
            }

            for (Livro livro : this.livros) {
                System.out.println(livro);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar os livros: " + e.getMessage());
        }
    }
}

class BibliotecaGUI extends JFrame {
    private Biblioteca biblioteca;

    private JButton btnAdicionar, btnRemover, btnAlterar, btnListar;
    private JTextField txtTitulo, txtAutor, txtId;
    private JTextArea txtAreaLivros;

    public BibliotecaGUI(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Biblioteca");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        txtTitulo = new JTextField(20);
        txtAutor = new JTextField(20);
        txtId = new JTextField(5);
        txtAreaLivros = new JTextArea(20, 30);
        txtAreaLivros.setEditable(false);

        btnAdicionar = new JButton("Adicionar Livro");
        btnRemover = new JButton("Remover Livro");
        btnAlterar = new JButton("Alterar Livro");
        btnListar = new JButton("Listar Livros");

        add(new JLabel("Título:"));
        add(txtTitulo);
        add(new JLabel("Autor:"));
        add(txtAutor);
        add(new JLabel("ID (para remover/alterar):"));
        add(txtId);
        add(btnAdicionar);
        add(btnRemover);
        add(btnAlterar);
        add(btnListar);
        add(new JScrollPane(txtAreaLivros));

        addActionListeners();
    }

    private void addActionListeners() {
        btnAdicionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                biblioteca.adicionarLivro(txtTitulo.getText(), txtAutor.getText());
                atualizarListaLivros();
            }
        });

        btnRemover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int id = Integer.parseInt(txtId.getText());
                    biblioteca.removerLivro(id);
                    atualizarListaLivros();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(BibliotecaGUI.this,
                            "ID inválido. Por favor, insira um número.");
                }
            }
        });

        btnAlterar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int id = Integer.parseInt(txtId.getText());
                    biblioteca.alterarLivro(id, txtTitulo.getText(), txtAutor.getText());
                    atualizarListaLivros();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(BibliotecaGUI.this,
                            "ID inválido. Por favor, insira um número.");
                }
            }
        });

        btnListar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarListaLivros();
            }
        });
    }

    private void atualizarListaLivros() {
        biblioteca.listarLivros(true);
        List<Livro> livros = biblioteca.livros;
        StringBuilder livrosTexto = new StringBuilder();

        for (Livro livro : livros) {
            livrosTexto.append(livro).append("\n");
        }

        txtAreaLivros.setText(livrosTexto.toString());
    }
}

public class Main {
    public static void main(String[] args) {
        Biblioteca biblioteca = new Biblioteca();
        SwingUtilities.invokeLater(() -> {
            BibliotecaGUI bibliotecaGUI = new BibliotecaGUI(biblioteca);
            bibliotecaGUI.setVisible(true);
        });
    }
}