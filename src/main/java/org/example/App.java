package org.example;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

class Okno extends JFrame {
    // dane do nawiązania komunikacji z bazą danych
    private String jdbcUrl = "jdbc:mysql://localhost:3306/Ksiegarnia?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", jdbcUser = "root", jdbcPass = "12345678";
    // pole na komunikaty od aplikacji
    private JTextField komunikat = new JTextField();
    // panel z zakładkami
    private JTabbedPane tp = new JTabbedPane();
    private JPanel panelKlienta = new JPanel(); // klienci
    private JPanel panelKsiazek = new JPanel(); // ksiązki
    private JPanel panelZamowienie = new JPanel(); // zamówiemia
    // panel dla zarządzania klientami
    private JTextField pole_pesel = new JTextField();
    private JTextField pole_im = new JTextField();
    private JTextField pole_naz = new JTextField();
    private JTextField pole_ur = new JTextField();
    private JTextField pole_mail = new JTextField();
    private JTextField pole_adr = new JTextField();
    private JTextField pole_tel = new JTextField();
    private JButton przyc_zapisz_kli = new JButton("zapisz");
    private JButton przyc_usun_kli = new JButton("usuń");
    private DefaultListModel<String> lmodel_kli = new DefaultListModel<>();
    private JList<String> l_kli = new JList<>(lmodel_kli);
    private JScrollPane sp_kli = new JScrollPane(l_kli);
    //panel dla zarzadzania ksiazkami
    private JTextField pole_isbn = new JTextField();
    private JTextField pole_autor = new JTextField();
    private JTextField pole_tytul = new JTextField();
    private JComboBox combo_typ = new JComboBox();
    private JTextField pole_wydawnictwo = new JTextField();
    private JTextField pole_rok = new JTextField();
    private JTextField pole_cena = new JTextField();

    private JButton przyc_zapisz_ksi = new JButton("zapisz");
    private JButton przyc_usun_ksi = new JButton("usuń");
    private DefaultListModel<String> lmodel_ksia = new DefaultListModel<>();
    private JList<String> l_ksia = new JList<>(lmodel_ksia);
    private JScrollPane sp_ksia = new JScrollPane(l_ksia);

    private JTextField pole_nowacena = new JTextField();
    private JButton button_nowacena = new JButton("Zmień cenę");

    private JTextField pole_data_zamowienie = new JTextField();
    private JComboBox comboTypZamowienia = new JComboBox();

    private DefaultListModel<String> listaModelKlientow = new DefaultListModel<>();
    private JList<String> listaKlientowZamowienia = new JList<>(listaModelKlientow);
    private JScrollPane scrollPaneKli_Zam = new JScrollPane(listaKlientowZamowienia);

    // lista zamawianych ksiazek

    private DefaultListModel<String> listModelKsiazekZamowienia = new DefaultListModel<>();
    private JList<String> listaKsiazekZamowienia = new JList<>(listModelKsiazekZamowienia);
    private JScrollPane scrollPaneKsiazkiZamowienia = new JScrollPane(listaKsiazekZamowienia);

    // lista zamowien

    private DefaultListModel<String> listModelZamowien = new DefaultListModel<>();
    private JList<String> listaZamowien = new JList<>(listModelZamowien);
    private JScrollPane scrollPaneZamowien = new JScrollPane(listaZamowien);

    // button "Dodaj zamowienie"
    private JButton buttonNoweZamowienie = new JButton("Dodaj nowe zamowienie");

    // button "Zmien status"
    private JButton buttonZmienStatus = new JButton("Zmien status");

    // funkcja aktualizująca listę klientów
    private void AktualnaListaKlientów(JList<String> lis) {
        try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT klienci.pesel, nazwisko, imie, adres FROM klienci, kontakty WHERE klienci.pesel = kontakty.pesel ORDER BY nazwisko, imie";
            ResultSet res = stmt.executeQuery(sql);
            lmodel_kli.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3) + ", " + res.getString(4);
                lmodel_kli.addElement(s);
            }
        }
        catch (SQLException ex) {
            komunikat.setText("nie udało się zaktualizować listy klientów "+ex);
            System.out.println(ex);
        }
    }
    // delegat obsługujący zdarzenie akcji od przycisku 'zapisz klienta'
    private ActionListener akc_zap_kli = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String pesel = pole_pesel.getText();
            if (! pesel.matches("[0-9]{3,11}")) {
                JOptionPane.showMessageDialog(Okno.this, "błąd w polu z peselm");
                pole_pesel.setText("");
                pole_pesel.requestFocus();
                return;
            }
            String imie = pole_im.getText();
            String nazwisko = pole_naz.getText();
            String ur = pole_ur.getText();
            if (imie.equals("") || nazwisko.equals("") || ur.equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "nie wypełnione pole z imieniem lub nazwiskiem lub datą urodzenia");
                return;
            }
            String mail = pole_mail.getText();
            String adr = pole_adr.getText();
            String tel = pole_tel.getText();
            if (mail.equals("") || adr.equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "nie wypełnione pole z emailem lub adresem");
                return;
            }
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql1 = "INSERT INTO klienci (pesel, imie, nazwisko, ur) VALUES('" + pole_pesel.getText() + "', '" + pole_im.getText() + "', '" + pole_naz.getText() + "', '" + pole_ur.getText() + "')";
                int res = stmt.executeUpdate(sql1);
                if (res == 1) {
                    komunikat.setText("OK - klient dodany do bazy");
                    String sql2 = "INSERT INTO kontakty (pesel, mail, adres, tel) VALUES('" + pole_pesel.getText() + "', '" + pole_mail.getText() + "', '" + pole_adr.getText() + "', '" + pole_tel.getText() + "')";
                    stmt.executeUpdate(sql2);
                    AktualnaListaKlientów(l_kli);
                }
            }
            catch(SQLException ex) {
                komunikat.setText("błąd SQL - nie zapisano klienta "+ex);
                System.out.print(ex);
            }
        }
    };
    // delegat obsługujący zdarzenie akcji od przycisku 'usuń klienta'
    private ActionListener akc_usun_kli = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (l_kli.getSelectedIndices().length == 0) return;
            String p = l_kli.getModel().getElementAt(l_kli.getSelectionModel().getMinSelectionIndex());
            p = p.substring(0, p.indexOf(':'));
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql = "SELECT COUNT(*) FROM zamowienia WHERE pesel = '" + p + "'";
                ResultSet res = stmt.executeQuery(sql);
                res.next();
                int k = res.getInt(1);
                if (k == 0) {
                    String sql1 = "DELETE FROM klienci WHERE pesel = '" + p + "'";
                    stmt.executeUpdate(sql1);
                    String sql2 = "DELETE FROM kontakty WHERE pesel = '" + p + "'";
                    stmt.executeUpdate(sql2);
                    komunikat.setText("OK - klient usunięty bazy");
                    AktualnaListaKlientów(l_kli);
                }
                else komunikat.setText("nie usunięto klienta, ponieważ składał już zamówienia");
            }
            catch (SQLException ex) {
                komunikat.setText("błąd SQL - nie ununięto klienta "+ex);
                System.out.println(ex);
            }
        }
    };

    //funkcja aktualizująca listę ksiazek
    private void AktualnaListaKsiazek() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT `isbn`, `autor`, `tytul`, `typ`, `rok`, `cena` FROM `ksiazki` WHERE 1 ORDER BY autor, tytul";
            ResultSet res = stmt.executeQuery(sql);
            lmodel_ksia.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + ": " + res.getString(3) + ": " + res.getString(4) + " " + res.getString(5) + " " + res.getString(6);
                lmodel_ksia.addElement(s);
            }
        }
        catch (SQLException ex) {
            komunikat.setText("nie udało się zaktualizować listy klientów");
        }
    }

    // delegat obsługujący zdarzenie akcji od przycisku 'zapisz ksiazki'
    private ActionListener akc_zap_ksia = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String ISBN = pole_isbn.getText();
            if (! ISBN.matches("[0-9]{3,11}")) {
                JOptionPane.showMessageDialog(Okno.this, "błąd w polu z ISBN");
                pole_isbn.setText("");
                pole_isbn.requestFocus();
                return;
            }
            String autor = pole_autor.getText();
            String tytul = pole_tytul.getText();
            Typ typ;
            typ = (Typ)combo_typ.getItemAt(combo_typ.getSelectedIndex());
            String typ_ksiazki = typ.name();
            if (autor.equals("") || tytul.equals("") || typ.equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "nie wypełnione pole z awtorem, tytulem lub typem książki");
                return;
            }
            String wydaw = pole_wydawnictwo.getText();
            String rok = pole_rok.getText();
            String cena = pole_cena.getText();
            if (cena.equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "nie wypełnione pole z ceną");
                return;
            }
            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sqlInsertKsia = "INSERT INTO `ksiazki`(`isbn`, `autor`, `tytul`, `typ`, `wydawnictwo`, `rok`, `cena`) VALUES ('" + ISBN + "','" + autor + "','" + tytul + "','" + typ + "','" + wydaw + "','" + rok + "','" + cena + "')";
                int res = stmt.executeUpdate(sqlInsertKsia);
                if (res == 1) {
                    komunikat.setText("OK - książka dodana do bazy");
                    AktualnaListaKsiazek();
                }
            }
            catch(SQLException ex) {
                komunikat.setText("błąd SQL - nie zapisano klienta: " + ex);
            }
        }
    };

    private ActionListener akc_usun_ksia = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (l_ksia.getSelectedIndices().length == 0) return;
            String p = l_ksia.getModel().getElementAt(l_ksia.getSelectionModel().getMinSelectionIndex());
            p = p.substring(0, p.indexOf(':'));
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                ///??????
                String sql = "SELECT COUNT(*) FROM zestawienia WHERE isbn = '" + p + "'";
                ResultSet res = stmt.executeQuery(sql);
                res.next();
                int k = res.getInt(1);
                if (k == 0) {
                    String sql1 = "DELETE FROM ksiazki WHERE isbn = '" + p + "'";
                    stmt.executeUpdate(sql1);
                    komunikat.setText("OK - książka usunięta z bazy");
                    AktualnaListaKsiazek();
                }
                else komunikat.setText("nie usunięto książkę, bo jest w zestawieniu");
            }
            catch (SQLException ex) {
                komunikat.setText("błąd SQL - nie ununięto książkę "+ex);
                System.out.println(ex);
            }
        }
    };

    private ActionListener zmiana_ceny_ksiazki = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String nowacena = pole_nowacena.getText();
            if (l_ksia.getSelectedIndices().length == 0 || nowacena.equals(""))
                return;
            String p = l_ksia.getModel().getElementAt(l_ksia.getSelectionModel().getMinSelectionIndex());
            p = p.substring(0, p.indexOf(':'));
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql = "UPDATE `ksiazki` SET `cena`='" + nowacena + "' WHERE `isbn`='" + p + "'";

                int res = stmt.executeUpdate(sql);
                if (res == 1) {
                    komunikat.setText("OK - cena książki zmieniona");
                    AktualnaListaKsiazek();
                }
                else {
                    komunikat.setText("nie zmieniono cenę ksiazki");
                }
            }
            catch (SQLException ex) {
                komunikat.setText("błąd SQL - nie ununięto książkę "+ex);
                System.out.println(ex);
            }
        }
    };

    //funkcja aktualizująca listę klientów

    private void AktualnaListaKlientówZamow() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT klienci.pesel, nazwisko, imie FROM klienci ORDER BY nazwisko, imie";
            ResultSet res = stmt.executeQuery(sql);
            lmodel_kli.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3);
                listaModelKlientow.addElement(s);
            }
        }
        catch (SQLException ex) {
            komunikat.setText("nie udało się zaktualizować listy klientów");
        }
    };
    private void AktualnaListaZamowionychKsiazek() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT isbn, autor, tytul, cena FROM ksiazki ORDER BY tytul";
            ResultSet res = stmt.executeQuery(sql);
            listModelKsiazekZamowienia.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3);
                listModelKsiazekZamowienia.addElement(s);
            }
        }
        catch (SQLException ex) {
            komunikat.setText("nie udało się zaktualizować listy klientów");
        }
    };
    private void AktualnaListaZamowien() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            // TO:DO add counter for order books
            String sql = "SELECT `id`, `pesel`, `kiedy`, `status` FROM `zamowienia` ORDER BY `kiedy`";
            ResultSet res = stmt.executeQuery(sql);
            listModelZamowien.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3) + " " + res.getString(4);
                listModelZamowien.addElement(s);
            }
        }
        catch (SQLException ex) {
            komunikat.setText("nie udało się zaktualizować listy klientów");
        }
    };

    private ActionListener akc_add_zamowienie = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String dataZamowienia = pole_data_zamowienie.getText();
            if (!dataZamowienia.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}")){
                JOptionPane.showMessageDialog(Okno.this, "Data [rok-miesiec-dzien] np.2020-05-17");
                pole_data_zamowienie.setText("");
                pole_data_zamowienie.requestFocus();
                return;
            }

            String orderStatus = "oczekuje";

            if (listaKsiazekZamowienia.getSelectedIndices().length == 0 || listaKlientowZamowienia.getSelectedIndices().length == 0)
                return;

            String p = listaKlientowZamowienia.getModel().getElementAt(listaKlientowZamowienia.getSelectionModel().getMinSelectionIndex());
            System.out.println("\nSelected Client from JList for create an order:  " + p);
            String orderClientKey = p.substring(0, p.indexOf(':'));
            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();

                String sqlInsertOrder = "INSERT INTO `zamowienia`(`pesel`, `kiedy`, `status`) VALUES ('" + orderClientKey + "', '" + dataZamowienia + "', '" + orderStatus + "')";
                int resInsertOrder = stmt.executeUpdate(sqlInsertOrder);

                String sqlSelectOrderKey = "SELECT `id` FROM `zamowienia` WHERE `pesel`= " + orderClientKey + " AND `kiedy` = '" + dataZamowienia + "' AND `status` = '" + orderStatus + "'";
                ResultSet resSelectOrderKey = stmt.executeQuery(sqlSelectOrderKey);
                resSelectOrderKey.next();
                int orderKey = resSelectOrderKey.getInt(1);
                System.out.println("\n Added order with PK: " + orderKey);

                System.out.println("\n Selected " + listaKsiazekZamowienia.getSelectedIndices().length + " books for order\n");
                for (int i = 0; i < listaKsiazekZamowienia.getSelectedIndices().length; i++) {
                    String bookToOrder = listaKsiazekZamowienia.getModel().getElementAt(i);
                    System.out.println(i+1 + " selected book from JList for order:  " + bookToOrder);

                    String keyBookToOrder = bookToOrder.substring(0, bookToOrder.indexOf(':'));
                    System.out.println("Key ordered book: " + keyBookToOrder);
                    String sqlSelectOrderBookPrice = "SELECT `cena` FROM `ksiazki` WHERE `isbn`=" + keyBookToOrder;
                    ResultSet resSelectOrderBookPrice = stmt.executeQuery(sqlSelectOrderBookPrice);
                    resSelectOrderBookPrice.next();
                    double orderBookPrice = resSelectOrderBookPrice.getDouble(1);
                    System.out.println("  witch price: " + orderBookPrice);

                    System.out.println("INSERT INTO `zestawienia`(`id`, `isbn`, `cena`) VALUES (`" + orderKey + "`, `" + keyBookToOrder + "` ,`" + orderBookPrice + "`)");
                    String sqlInsertOrderBook = "INSERT INTO `zestawienia`(`id`, `isbn`, `cena`) VALUES (" + orderKey + ", " + keyBookToOrder + " ," + orderBookPrice + ")";
                    int resInsertOrderBook = stmt.executeUpdate(sqlInsertOrderBook);
                    System.out.println(i + " book added result: " + resInsertOrderBook);
                }

                if (resInsertOrder == 1) {
                    komunikat.setText("OK - cena książki zmieniona");
                    AktualnaListaZamowien();
                } else {
                    komunikat.setText("nie zmieniono cenę ksiazki");
                }
            } catch (SQLException ex) {
                komunikat.setText("błąd SQL - nie ununięto ksiazke");
            }
        }
    };
    private ActionListener akc_add_zmien_status = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Status status;
            status = (Status)comboTypZamowienia.getItemAt(comboTypZamowienia.getSelectedIndex());
            String orderStatus = status.name();

            komunikat.setText(listaZamowien.getModel().getElementAt(listaZamowien.getSelectionModel().getMinSelectionIndex()));
            if (listaZamowien.getSelectedIndices().length == 0)
                return;
            String selectedOrder = listaZamowien.getModel().getElementAt(listaZamowien.getSelectionModel().getMinSelectionIndex());
            System.out.println("\nSelected item from JList to change status:  " + selectedOrder);
            String keyOrder = selectedOrder.substring(0, selectedOrder.indexOf(':'));

            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                System.out.println("UPDATE `zamowienia` SET `status`='" + orderStatus + "' WHERE `id`='" + keyOrder + "'");
                String sql = "UPDATE `zamowienia` SET `status`='" + orderStatus + "' WHERE `id`='" + keyOrder + "'";
                int res = stmt.executeUpdate(sql);
                if (res == 1) {
                    komunikat.setText("OK - status zamówienia zmieniony");
                    AktualnaListaZamowien();
                } else {
                    komunikat.setText("nie zmieniono statusu zamuwienia");
                }
            } catch (SQLException ex) {
                komunikat.setText("błąd SQL - nie zmieniono statusu zamuwienia");
            }
        }
    };




    public Okno() throws SQLException {
        super("Księgarnia wysyłkowa");
        setSize(660, 460);
        setLocation(100, 100);
        setResizable(false);
        // panel do zarządzania klientami
        panelKlienta.setLayout(null);
        // pole z peselem
        JLabel lab1 = new JLabel("pesel:");
        panelKlienta.add(lab1);
        lab1.setSize(100, 20);
        lab1.setLocation(40, 40);
        lab1.setHorizontalTextPosition(JLabel.RIGHT);
        panelKlienta.add(pole_pesel);
        pole_pesel.setSize(200, 20);
        pole_pesel.setLocation(160, 40);
        // pole z imieniem
        JLabel lab2 = new JLabel("imię:");
        panelKlienta.add(lab2);
        lab2.setSize(100, 20);
        lab2.setLocation(40, 80);
        lab2.setHorizontalTextPosition(JLabel.RIGHT);
        panelKlienta.add(pole_im);
        pole_im.setSize(200, 20);
        pole_im.setLocation(160, 80);
        // pole z nazwiskiem
        JLabel lab3 = new JLabel("nazwisko:");
        panelKlienta.add(lab3);
        lab3.setSize(100, 20);
        lab3.setLocation(40, 120);
        lab3.setHorizontalTextPosition(JLabel.RIGHT);
        panelKlienta.add(pole_naz);
        pole_naz.setSize(200, 20);
        pole_naz.setLocation(160, 120);
        // pole z datą urodzenia
        JLabel lab4 = new JLabel("data urodzenia:");
        panelKlienta.add(lab4);
        lab4.setSize(100, 20);
        lab4.setLocation(40, 160);
        lab4.setHorizontalTextPosition(JLabel.RIGHT);
        panelKlienta.add(pole_ur);
        pole_ur.setSize(200, 20);
        pole_ur.setLocation(160, 160);
        // pole z mailem
        JLabel lab5 = new JLabel("mail:");
        panelKlienta.add(lab5);
        lab5.setSize(100, 20);
        lab5.setLocation(40, 200);
        lab5.setHorizontalTextPosition(JLabel.RIGHT);
        panelKlienta.add(pole_mail);
        pole_mail.setSize(200, 20);
        pole_mail.setLocation(160, 200);
        // pole z adresem
        JLabel lab6 = new JLabel("adres:");
        panelKlienta.add(lab6);
        lab6.setSize(100, 20);
        lab6.setLocation(40, 240);
        lab6.setHorizontalTextPosition(JLabel.RIGHT);
        panelKlienta.add(pole_adr);
        pole_adr.setSize(200, 20);
        pole_adr.setLocation(160, 240);
        // pole z telefonem
        JLabel lab7 = new JLabel("telefon:");
        panelKlienta.add(lab7);
        lab7.setSize(100, 20);
        lab7.setLocation(40, 280);
        lab7.setHorizontalTextPosition(JLabel.RIGHT);
        panelKlienta.add(pole_tel);
        pole_tel.setSize(200, 20);
        pole_tel.setLocation(160, 280);
        // przycisk do zapisu klienta
        panelKlienta.add(przyc_zapisz_kli);
        przyc_zapisz_kli.setSize(200, 20);
        przyc_zapisz_kli.setLocation(160, 320);
        przyc_zapisz_kli.addActionListener(akc_zap_kli);
        // przycisk do usunięcia klienta
        panelKlienta.add(przyc_usun_kli);
        przyc_usun_kli.setSize(200, 20);
        przyc_usun_kli.setLocation(400, 320);
        przyc_usun_kli.addActionListener(akc_usun_kli);
        // lista z klientami
        panelKlienta.add(sp_kli);
        sp_kli.setSize(200, 260);
        sp_kli.setLocation(400, 40);
        l_kli.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AktualnaListaKlientów(l_kli);
        // panel z zakładkami
        tp.addTab("klienci", panelKlienta);
        tp.addTab("książki", panelKsiazek);
        tp.addTab("zamówienia", panelZamowienie);
        getContentPane().add(tp, BorderLayout.CENTER);
        // pole na komentarze
        komunikat.setEditable(false);
        getContentPane().add(komunikat, BorderLayout.SOUTH);
        // pokazanie okna
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        // panel do zarządzania ksiazkami
        panelKsiazek.setLayout(null);

        // pole z numerem isbn
        JLabel labelISBN = new JLabel("ISBN:");
        panelKsiazek.add(labelISBN);
        labelISBN.setSize(100, 20);
        labelISBN.setLocation(40, 30);
        labelISBN.setHorizontalTextPosition(JLabel.RIGHT);

        panelKsiazek.add(pole_isbn);
        pole_isbn.setSize(200, 20);
        pole_isbn.setLocation(160, 30);

        // pole z autorem
        JLabel labelAutor = new JLabel("autor:");
        panelKsiazek.add(labelAutor);
        labelAutor.setSize(100, 20);
        labelAutor.setLocation(40, 70);
        labelAutor.setHorizontalTextPosition(JLabel.RIGHT);

        panelKsiazek.add(pole_autor);
        pole_autor.setSize(200, 20);
        pole_autor.setLocation(160, 70);

        // pole z tytulem
        JLabel labelTytul = new JLabel("tytul:");
        panelKsiazek.add(labelTytul);
        labelTytul.setSize(100, 20);
        labelTytul.setLocation(40, 110);
        labelTytul.setHorizontalTextPosition(JLabel.RIGHT);

        panelKsiazek.add(pole_tytul);
        pole_tytul.setSize(200, 20);
        pole_tytul.setLocation(160, 110);

        // pole z typem
        JLabel labelTyp = new JLabel("typ:");
        panelKsiazek.add(labelTyp);
        labelTyp.setSize(100, 20);
        labelTyp.setLocation(40, 150);
        labelTyp.setHorizontalTextPosition(JLabel.RIGHT);

        panelKsiazek.add(combo_typ);
        combo_typ.setSize(200, 20);
        combo_typ.setLocation(160, 150);

        for (Typ typ:Typ.values()
             ) {
            combo_typ.addItem(typ);
        }

        // pole z wydawnictwem
        JLabel labelWydawnictwo = new JLabel("wydawnictwo:");
        panelKsiazek.add(labelWydawnictwo);
        labelWydawnictwo.setSize(100, 20);
        labelWydawnictwo.setLocation(40, 190);
        labelWydawnictwo.setHorizontalTextPosition(JLabel.RIGHT);

        panelKsiazek.add(pole_wydawnictwo);
        pole_wydawnictwo.setSize(200, 20);
        pole_wydawnictwo.setLocation(160, 190);

        // pole z rokiem
        JLabel labelRok = new JLabel("rok:");
        panelKsiazek.add(labelRok);
        labelRok.setSize(100, 20);
        labelRok.setLocation(40, 230);
        labelRok.setHorizontalTextPosition(JLabel.RIGHT);

        panelKsiazek.add(pole_rok);
        pole_rok.setSize(200, 20);
        pole_rok.setLocation(160, 230);

        // pole z cena
        JLabel labelCena = new JLabel("cena:");
        panelKsiazek.add(labelCena);
        labelCena.setSize(100, 20);
        labelCena.setLocation(40, 270);
        labelCena.setHorizontalTextPosition(JLabel.RIGHT);

        panelKsiazek.add(pole_cena);
        pole_cena.setSize(200, 20);
        pole_cena.setLocation(160, 270);

        // lista z ksiazkami
        panelKsiazek.add(sp_ksia);
        sp_ksia.setSize(200, 260);
        sp_ksia.setLocation(400, 40);
        l_ksia.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AktualnaListaKsiazek();

        // button to add book
        panelKsiazek.add(przyc_zapisz_ksi);
        przyc_zapisz_ksi.setSize(200, 20);
        przyc_zapisz_ksi.setLocation(160, 310);
        przyc_zapisz_ksi.addActionListener(akc_zap_ksia);

        // button to remove book
        panelKsiazek.add(przyc_usun_ksi);
        przyc_usun_ksi.setSize(200, 20);
        przyc_usun_ksi.setLocation(400, 310);
        przyc_usun_ksi.addActionListener(akc_usun_ksia);

        // nowa cena
        JLabel label_nowacena = new JLabel("nowa cena:");
        panelKsiazek.add(label_nowacena);
        label_nowacena.setSize(100, 20);
        label_nowacena.setLocation(40, 340);
        label_nowacena.setHorizontalTextPosition(JLabel.RIGHT);

        panelKsiazek.add(pole_nowacena);
        pole_nowacena.setSize(200, 20);
        pole_nowacena.setLocation(160, 340);

        panelKsiazek.add(button_nowacena);
        button_nowacena.setSize(200, 20);
        button_nowacena.setLocation(400, 340);
        button_nowacena.addActionListener(zmiana_ceny_ksiazki);

        // panel zamów
        panelZamowienie.setLayout(null);

        JLabel labelData_zamowienia = new JLabel("data zamowienia:");
        panelZamowienie.add(labelData_zamowienia);
        labelData_zamowienia.setSize(100, 20);
        labelData_zamowienia.setLocation(10, 10);
        labelData_zamowienia.setHorizontalTextPosition(JLabel.RIGHT);

        panelZamowienie.add(pole_data_zamowienie);
        pole_data_zamowienie.setSize(100, 20);
        pole_data_zamowienie.setLocation(110, 10);

        JLabel labelTyp_zamowienia = new JLabel("zmien status zamowienia:");
        panelZamowienie.add(labelTyp_zamowienia);
        labelTyp_zamowienia.setSize(200, 20);
        labelTyp_zamowienia.setLocation(330, 210);
        labelTyp_zamowienia.setHorizontalTextPosition(JLabel.RIGHT);

        panelZamowienie.add(comboTypZamowienia);
        comboTypZamowienia.setSize(110, 20);
        comboTypZamowienia.setLocation(522,210);
        for(Status status: Status.values()) {
            comboTypZamowienia.addItem(status);
        }

        // Client comboBox

        panelZamowienie.add(scrollPaneKli_Zam);
        scrollPaneKli_Zam.setSize(300, 150);
        scrollPaneKli_Zam.setLocation(10, 40);
        l_kli.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AktualnaListaKlientówZamow();

        // lista zamawianych ksiazek

        panelZamowienie.add(scrollPaneKsiazkiZamowienia);
        scrollPaneKsiazkiZamowienia.setSize(300, 150);
        scrollPaneKsiazkiZamowienia.setLocation(330, 40);
        l_ksia.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        AktualnaListaZamowionychKsiazek();

        // lista zamowien

        panelZamowienie.add(scrollPaneZamowien);
        scrollPaneZamowien.setSize(300, 150);
        scrollPaneZamowien.setLocation(10, 200);
        l_ksia.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AktualnaListaZamowien();

        // button "Dodaj zamowienie"
        panelZamowienie.add(buttonNoweZamowienie);
        buttonNoweZamowienie.setSize(200, 20);
        buttonNoweZamowienie.setLocation(375,300);
        buttonNoweZamowienie.addActionListener(akc_add_zamowienie);

        // button "Zmien status"
        panelZamowienie.add(buttonZmienStatus);
        buttonZmienStatus.setSize(200, 20);
        buttonZmienStatus.setLocation(375,260);
        buttonZmienStatus.addActionListener(akc_add_zmien_status);
    }
}

public class App {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        new Okno();
    }
}

