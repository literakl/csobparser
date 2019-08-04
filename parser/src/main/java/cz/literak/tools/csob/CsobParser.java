package cz.literak.tools.csob;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

public class CsobParser {
    private static List<BankAccount> myAccounts = new ArrayList<BankAccount>(3);
    private static Pattern pattern;
    static {
        pattern = Pattern.compile("(Částka: )([\\d\\.]+ [\\p{Upper}]{3}) ([\\d\\.]+)");
        myAccounts.add(new BankAccount("1234", "3030")); // spořící účet
        myAccounts.add(new BankAccount("1234", "3030")); // podnikatelský účet
        myAccounts.add(new BankAccount("1234", "0300")); // běžný účet
        myAccounts.add(new BankAccount("1234", "0300")); // kreditka
        myAccounts.add(new BankAccount("1234", "0300")); // kontokorent
    }

    public static void main(String[] args) throws Exception {
        try (PrintWriter writer = new PrintWriter("transactions.csv")) {
            for (String arg : args) {
                File file = new File(arg);
                SAXReader reader = new SAXReader();
                reader.setEncoding("Windows-1250");
                Document doc = reader.read(file);
                Element finsta03 = doc.getRootElement().element("FINSTA03");
                List<Element> elements = finsta03.elements("FINSTA05");
                for (Element el : elements) {
                    String accountNumber = el.elementText("PART_ACCNO");
                    String bankNumber = el.elementText("PART_BANK_ID");
                    String accountName = el.elementText("PART_ACC_ID");
                    String date = el.elementText("S61_DATUM"), date2 = "";
                    String amount = el.elementText("S61_CASTKA"), amount2 = "";
                    String currency = el.elementText("S61_MENA");
                    String note1 = el.elementText("PART_ID1_1").trim();
                    String note2 = el.elementText("PART_ID1_2").trim();
                    if (note1.startsWith("Místo")) {
                        note1 = note1.substring(7);
                    }
                    Matcher matcher = pattern.matcher(note2);
                    if (matcher.matches()) {
                        amount2 = matcher.group(2);
                        date2 = matcher.group(3);
                        note2 = "";
                    }
                    if (! isMyAccount(accountNumber, bankNumber)) {
                        writer.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s\n", date, date2, amount, currency, amount2, accountNumber, bankNumber, accountName, note1, note2);
                    }
                }
            }
        }
    }

    static boolean isMyAccount(String accountNumber, String bankNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            return false;
        }
        if (bankNumber == null || bankNumber.isEmpty()) {
            return false;
        }
        for (BankAccount myAccount : myAccounts) {
            if (myAccount.accountNumber.equals(accountNumber) && myAccount.bankNumber.equals(bankNumber)) {
                return true;
            }
        }
        return false;
    }
}
