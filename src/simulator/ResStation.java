package simulator;

/**
 * Created by bob35 on 2017/6/8.
 */
public class ResStation {
    private int id;
    private int Qj, Qk;
    private float Vj, Vk;
    private int A;
    private String op;
    private boolean busy;
    private Instruction ins;
    public int getId(){
      return id;
    }
    
    public void reset(){
        Qj = Qk = A = 0;
        Vj = Vk = 0;
        op = "";
        busy = false;
        ins = null;
    }

    public ResStation(String op, Instruction ins){
        this(0, 0, 0, 0, 0, op, false, ins);
    }

    public ResStation(int qj, int qk, float vj, float vk, int a, String op, boolean busy, Instruction ins) {
        Qj = qj;
        Qk = qk;
        Vj = vj;
        Vk = vk;
        A = a;
        this.op = op;
        this.busy = busy;
        this.ins = ins;
    }


    public int getQk() {
        return Qk;
    }

    public void setQk(int qk) {
        Qk = qk;
    }

    public float getVj() {
        return Vj;
    }

    public void setVj(float vj) {
        Vj = vj;
    }

    public float getVk() {
        return Vk;
    }

    public void setVk(float vk) {
        Vk = vk;
    }

    public int getA() {
        return A;
    }

    public void setA(int a) {
        A = a;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public Instruction getIns() {
        return ins;
    }

    public void setIns(Instruction ins) {
        this.ins = ins;
    }

    public int getQj() {
        return Qj;
    }

    public void setQj(int qj) {
        Qj = qj;
    }
}
