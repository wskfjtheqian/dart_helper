interface T {
    void call();
}

public class Aa {


    public void B(T b) {

    }

    public Aa(String vv) {
        B(() -> {

        });
    }
}
