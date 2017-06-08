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

    private ResStation []addResStation;
    private ResStation []mulResStation;
    private ResStation []ldResStation;
    private ResStation []stResStation;

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
                        regs.setQi(instruction.getRd(), i);
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

                    regs.setQi(instruction.getRd(), i);
                }
                currentIns++;
                break;
            }
        }
    }

    private void exec(){

    }

    private void update(){

    }

    public boolean done(){
        for (Instruction ins : instructionVector)
            if (ins.getState() < 3)
                return false;
        return true;
    }

}
