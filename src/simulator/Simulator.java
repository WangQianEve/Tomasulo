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
        int id = 0;
        addResStation = new ResStation[3];
        for (ResStation resStation : addResStation){
            resStation.setId(id);
            id++;
        }
        mulResStation = new ResStation[2];
        for (ResStation resStation : mulResStation){
            resStation.setId(id);
            id++;
        }
        ldResStation = new ResStation[3];
        for (ResStation resStation : ldResStation){
            resStation.setId(id);
            id++;
        }
        stResStation = new ResStation[3];
        for (ResStation resStation : stResStation){
            resStation.setId(id);
            id++;
        }
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
            }
        }
    }

    private void print_state() {
        System.out.println("clock = " + clock);
        for (Instruction ins : instructionVector)
            ins.print();
    }

    private void print_regs(){
        for (int i = 0; i < regSize; i++)
            System.out.printf("Registers: Q[%d]: %d, Value[%d]: %f%n", i, regs.getQi(i), i, regs.getValue(i));
    }

    private void print_rs(){
        int label = 0;
        for (ResStation res : addResStation) {
            System.out.printf("RS #%d, Op: %s, Qj: %d, Qk: %d, Vj: %d, Vk: %d, busy: %d, A: %d%n",
                    label, res.getOp(), res.getQj(), res.getQk(), res.getVj(), res.getVk(), res.isBusy(), res.getA());
            label++;
        }
        for (ResStation res : mulResStation) {
            System.out.printf("RS #%d, Op: %s, Qj: %d, Qk: %d, Vj: %d, Vk: %d, busy: %d, A: %d%n",
                    label, res.getOp(), res.getQj(), res.getQk(), res.getVj(), res.getVk(), res.isBusy(), res.getA());
            label++;
        }
        for (ResStation res : ldResStation) {
            System.out.printf("RS #%d, Op: %s, Qj: %d, Qk: %d, Vj: %d, Vk: %d, busy: %d, A: %d%n",
                    label, res.getOp(), res.getQj(), res.getQk(), res.getVj(), res.getVk(), res.isBusy(), res.getA());
            label++;
        }
        for (ResStation res : stResStation) {
            System.out.printf("RS #%d, Op: %s, Qj: %d, Qk: %d, Vj: %d, Vk: %d, busy: %d, A: %d%n",
                    label, res.getOp(), res.getQj(), res.getQk(), res.getVj(), res.getVk(), res.isBusy(), res.getA());
            label++;
        }
    }

    private void print_units(){
        System.out.printf("add unit: rs_id: %d, result: %f, busy: %d, end_time: %d%n",
                adder.getRs_id(), adder.getResult(), adder.isBusy(), adder.getEnd_time());

        System.out.printf("mul unit: rs_id: %d, result: %f, busy: %d, end_time: %d%n",
                multiplier.getRs_id(), multiplier.getResult(), multiplier.isBusy(), multiplier.getEnd_time());
    }

    public void step(){
        clock += 1;
        update();
        exec();
        influx();
    }

    private ResStation[] getResStations(String op){
        ResStation []resStations;
        switch (op){
            case "ADDD":

            case "SUBD":
                resStations = addResStation;
                break;

            case "MULD":

            case "DIVD":
                resStations = mulResStation;
                break;

            case "LD":
                resStations = ldResStation;
                break;

            case "ST":
                resStations = stResStation;
                break;

            default:
                resStations = null;
        }
        return resStations;
    }

    /**
     * function: influx a new instruction
     */
    private void influx(){
        if (this.currentIns >= instructionVector.size())
            return ;
        Instruction instruction = instructionVector.get(currentIns);
        ResStation[] resStations = getResStations(instruction.getOp());
        for (int i = 0; i < resStations.length; i++) {
            ResStation resStation = resStations[i];
            if (!resStation.isBusy()) {
                resStation.setBusy(true);
                resStation.setIns(instruction);
                resStation.setOp(instruction.getOp());
                instruction.setState(1);
                if ((instruction.getOp() == "LD") || (instruction.getOp() == "ST")) {
                    resStation.setA(instruction.getRs());
                    if (instruction.getOp() == "LD") {
                        regs.setQi(instruction.getRd(), resStation.getId());
                    } else { //STORE
                        if (regs.getQi(instruction.getRd()) == 0) {
                            resStation.setQj(0);
                            resStation.setVj(regs.getValue(instruction.getRd()));
                        }
                        else {
                            resStation.setQj(regs.getQi(instruction.getRd()));
                        }
                    }
                    this.memory_queue.add(resStation);
                } else {// ADDD SUBD MULD DIVD
                    if (regs.getQi(instruction.getRs()) == 0){
                        resStation.setQj(0);
                        resStation.setVj(regs.getValue(instruction.getRs()));
                    }
                    else
                        resStation.setQj(regs.getQi(instruction.getRs()));

                    if (regs.getQi(instruction.getRt()) == 0){
                        resStation.setQk(0);
                        resStation.setVk(regs.getValue(instruction.getRt()));
                    }
                    else
                        resStation.setQk(regs.getQi(instruction.getRt()));

                    regs.setQi(instruction.getRd(), resStation.getId());
                }
                currentIns++;
                break;
            }
        }
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

    private void updateResStation(ResStation[] resStations, int id, float result){
        for (ResStation resStation : resStations) {
            if (resStation.getQj() == id){
                resStation.setQj(0);
                resStation.setVj(result);
            }
            if (resStation.getQk() == id){
                resStation.setQk(0);
                resStation.setVk(result);
            }
        }
    }

    private void update(){
        //load&store
        if (memory_unit.getEnd_time() == clock){
            ResStation resStation = memory_queue.firstElement();
            Instruction instruction = resStation.getIns();
            if (instruction.getOp() == "LD") {
                regs.setQi(instruction.getRd(), 0);
                regs.setValue(instruction.getRd(), memory_unit.getResult());
                updateResStation(addResStation, resStation.getId(), memory_unit.getResult());
                updateResStation(mulResStation, resStation.getId(), memory_unit.getResult());
                updateResStation(ldResStation, resStation.getId(), memory_unit.getResult());
                updateResStation(stResStation, resStation.getId(), memory_unit.getResult());
            }
            else {
                mems.setValue(resStation.getA()/4, memory_unit.getResult());
            }
            resStation.setBusy(false);
            instruction.setState(3);
        }
        //adder
        if (adder.getEnd_time() == clock) {
            ResStation resStation = addResStation[adder.getRs_id()];
            for (int i = 0; i < regSize; i++){
                if (regs.getQi(i) == adder.getRs_id()){
                    regs.setQi(i, 0);
                    regs.setValue(i, adder.getResult());
                }
            }
            updateResStation(addResStation, resStation.getId(), adder.getResult());
            updateResStation(mulResStation, resStation.getId(), adder.getResult());
            updateResStation(ldResStation, resStation.getId(), adder.getResult());
            updateResStation(stResStation, resStation.getId(), adder.getResult());
            resStation.setBusy(false);
            resStation.getIns().setState(3);
            adder.setBusy(false);
        }
        //multi
        if (multiplier.getEnd_time() == clock) {
            ResStation resStation = mulResStation[multiplier.getRs_id() - 3];
            for (int i = 0; i < regSize; i++){
                if (regs.getQi(i) == resStation.getId()){
                    regs.setQi(i, 0);
                    regs.setValue(i, multiplier.getResult());
                }
            }
            updateResStation(addResStation, resStation.getId(), multiplier.getResult());
            updateResStation(mulResStation, resStation.getId(), multiplier.getResult());
            updateResStation(ldResStation, resStation.getId(), multiplier.getResult());
            updateResStation(stResStation, resStation.getId(), multiplier.getResult());
            resStation.setBusy(false);
            resStation.getIns().setState(3);
            multiplier.setBusy(false);
        }
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
