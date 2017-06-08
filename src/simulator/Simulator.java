package simulator;

import java.util.Vector;

/**
 * Created by bob35 on 2017/6/8.
 */
public class Simulator {

    private int clock;
    /**
     * currentIns : current instruction to influx
     */
    private Vector<Instruction> instructionVector;
    private int currentIns;

    private int regSize;
    private Registers regs;
    private int memSize;
    private Memory mems;

    private ResStation []addResStation;//0-2
    private ResStation []mulResStation;//3-4
    private ResStation []ldResStation;//5-7
    private ResStation []stResStation;//8-10

    private Adder adder;
    private Multiplier multiplier;
    private Memory_unit memory_unit;

    private Vector<ResStation> memory_queue;

    public Simulator() {
        clock = 0;
        currentIns = 0;
        instructionVector = new Vector<>();
        regSize = 32;
        memSize = 1024;
        regs = new Registers(regSize);
        mems = new Memory(memSize);
        addResStation = new ResStation[3];
        mulResStation = new ResStation[2];
        ldResStation = new ResStation[3];
        stResStation = new ResStation[3];
        adder = new Adder();
        multiplier = new Multiplier();
        memory_unit = new Memory_unit();
        memory_queue = new Vector<>();
    }

    public void reset(){
        clock = 0;
        currentIns = 0;
        for(Instruction ins : instructionVector)
            ins.reset();
        regs.reset();
        mems.reset();
        for(ResStation rs : addResStation) rs.reset();
        for(ResStation rs : mulResStation) rs.reset();
        for(ResStation rs : ldResStation) rs.reset();
        for(ResStation rs : stResStation) rs.reset();
        adder.reset();
        multiplier.reset();
        memory_unit.reset();
        memory_queue.clear();
    }

    public void readInstruction(String inst_txt){
        String[] ins_lines = inst_txt.split("\n");
        for (String ins_line : ins_lines){
            Instruction ins = new Instruction(ins_line);
            instructionVector.add(ins);
        }
    }

    public void writeMemory(int index, float v){
        mems.setValue(index, v);
    }

    public float readMemory(int index){
        return mems.getValue(index);
    }

    public void run(boolean debug){
        this.reset();
        while (!this.done()){
            step();
            if (debug){
                print_state();
                print_regs();
                print_rs();
                print_units();
                print_mems();
            }
        }
    }

    private void print_state() {
    }

    private void print_regs(){

    }

    private void print_rs(){

    }

    private void print_units(){

    }

    private void print_mems(){

    }

    public void step(){

    }

    private void influx(){

    }

    private void exec(){
      ResStation curRes;
      if(memory_queue.size()>0){
        curRes = memory_queue.get(0);
        if(curRes.getIns().getState()<2){
          if(curRes.getOp()=="LD"){
            memory_unit.setResult(mems.getValue(curRes.getA()));
            curRes.getIns().setState(2);
            memory_unit.setRs_id(curRes.getId());
            memory_unit.setEnd_time(clock+2);
          }else{
            if(curRes.getQj()==0){
              memory_unit.setResult(curRes.getVj());
              curRes.getIns().setState(2);
              memory_unit.setRs_id(curRes.getId());
              memory_unit.setEnd_time(clock+2);
            }
          }
        }
      }
      if(!adder.isBusy()){
        for(ResStation resStation: addResStation){
          if(resStation.getQj()==0 && resStation.getQk()==0 && resStation.isBusy()){
            float a = resStation.getVj();
            float b = resStation.getVk();
            if(resStation.getOp()=="ADDD"){
              adder.setResult(a+b);
            }
            else{
              adder.setResult(a-b);
            }
            System.out.println("Add result: "+adder.getResult());
            adder.setRs_id(resStation.getId());
            adder.setEnd_time(clock+2);
            adder.setBusy(true);
            resStation.getIns().setState(2);
            break;
          }
        }
      }
      if(!multiplier.isBusy()){
        for(ResStation resStation: mulResStation){
          if(resStation.getQj()==0 && resStation.getQk()==0 && resStation.isBusy()){
            float a = resStation.getVj();
            float b = resStation.getVk();
            if(resStation.getOp()=="MULD"){
              multiplier.setResult(a*b);
              multiplier.setEnd_time(clock+10);
            }
            else{
              multiplier.setResult(a/b);
              multiplier.setEnd_time(clock+40);
            }
            System.out.println("Mul result: "+multiplier.getResult());
            multiplier.setRs_id(resStation.getId());
            multiplier.setBusy(true);
            resStation.getIns().setState(2);
            break;
          }
        }
      }
    }

    private void update(){

    }

    public boolean done(){
        for (Instruction ins : instructionVector)
            if (ins.getState() < 3)
                return false;
        return true;
    }

    public static void main(String []args) {
        System.out.println("Hello World");
    }
}
