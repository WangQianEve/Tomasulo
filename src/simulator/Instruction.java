package simulator;

/**
 * Created by bob35 on 2017/6/8.
 */
public class Instruction {
    /**
     * state :
     * -1 - not legal
     *  0 - not flow in
     *  1 - influxed
     *  2 - exec
     *  3 - write result
     *  4 - halt
     * time_cost :
     * op :
     * rs, rt, rd : source1, source2, dest for ADD, SUB, MULTI, DIVD
     * rs, rd : Imm, register for LOAD, STORE
     */
    private int state;
    private int time_cost;
    private String op;
    private int rs, rt, rd;

    Instruction() {
        state = 0;
        time_cost = 0;
        op = "";
        rs = 0;
        rt = 0;
        rd = 0;
    }

    Instruction(String ins) {
        parse(ins);
    }

    public void reset(){
        state = 0;
    }

    private void parse(String ins) {
        this.state = 0;
        this.time_cost = 0;
        this.op = "";
        this.rs = this.rt = this.rd = 0;
        String str[];
        str = ins.split(" ");
        try{
            op = str[0];
            if (str.length == 4) {
                switch (op){
                    case "ADDD":

                    case "SUBD":
                        this.time_cost = 2;
                        break;
                    case "MULD":
                        this.time_cost = 10;
                        break;
                    case "DIVD":
                        this.time_cost = 40;
                        break;
                    default:
                        this.state = -1;
                        System.out.println("wrong instruction format : " + ins);
                }
                this.rd = Integer.parseInt(str[1].substring(1));
                this.rs = Integer.parseInt(str[2].substring(1));
                this.rt = Integer.parseInt(str[3].substring(1));
            }
            else if ((op.compareTo("LD") == 0) || (op.compareTo("ST") == 0)){
                this.rd = Integer.parseInt(str[1].substring(1));
                this.rs = Integer.parseInt(str[2]);
                this.time_cost = 2;
            }
            else {
                this.state = -1;
                System.out.println("wrong instruction format : " + ins);
            }

        }catch (Exception err)
        {
            this.state = -1;
            System.out.println("wrong instruction format : " + ins);
        }
    }

    public void print()
    {
        if (state == -1) {
            System.out.println("wrong instruction format");
            return;
        }
        System.out.printf("op = %s%n", op);
        System.out.printf("time_cost = %d%n", time_cost);
        System.out.printf("rs = %d%n", rs);
        System.out.printf("rt = %d%n", rt);
        System.out.printf("rd = %d%n", rd);
        System.out.printf("state = %d%n", state);
    }

    public int getRs(){
        return this.rs;
    }

    public int getRt(){
        return this.rt;
    }

    public int getRd(){
        return this.rd;
    }

    public int getTime_cost(){
        return this.time_cost;
    }

    public int getState(){
        return this.state;
    }

    public void setState(int state){
        this.state = state;
    }
}
