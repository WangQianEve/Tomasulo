package simulator;

/**
 * Created by bob35 on 2017/6/8.
 */
public class Main {
    public static void main(String args[]) {
        MyGUI mygui = new MyGUI();
        Simulator smu = new Simulator();
        smu.writeMemory(1024, 4);
        smu.writeMemory(1028, 8);
        String instructions = "LD F0 1024\n" +
                "LD F1 1028\n" +
                "ADDD F2 F0 F1\n" +
                "SUBD F3 F0 F1\n" +
                "MULD F4 F0 F1\n" +
                "DIVD F5 F0 F1\n" +
                "ST F0 0\n" +
                "ST F1 4\n" +
                "ST F2 8\n" +
                "ST F3 12\n" +
                "ST F4 16\n" +
                "ST F5 20";
        smu.readInstruction(instructions);
        smu.run(true, 0);
    }
}
