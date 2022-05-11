package Service;


public class Utilites {

    public Utilites() {
    }

    public boolean checkWords(String input) {
        return input.matches("^[[a-zA-Z]+[\\d]*]+$") && !input.isBlank();
    }

    public boolean checkEmail(String input) {
        return input.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }


}

