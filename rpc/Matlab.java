package rpc;

public class Matlab {
    private int i;

    public Matlab() {
        this.i = 0;
    }

    public void init(int i) {
        this.i = i;
    }

    public Result calcul(int in) {
        return new Result(in * this.i);
    }
}
