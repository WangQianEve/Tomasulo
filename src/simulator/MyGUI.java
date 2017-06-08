package simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Math;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.table.DefaultTableModel;


public class MyGUI {
	static Simulator smu;
	static final int WIDTH = 1200;
    static final int HEIGHT = 700;
    JFrame jf;
    JPanel root;

    //Tables
    JTable instListTable;
    JTable exeStatusTable;
    JTable loadQueueTable;
    JTable storeQueueTable;
    JTable reservationStationTable;
    JTable FUTable;
    JTable RUTable;
		JTextArea instructionText;
		JTextArea memText;
    //Labels
    JTextField instNumLabel;
    JLabel PCLabel;
    JLabel timeLabel;

    //Button
    JButton runBtn;
		JButton stepBtn;
    JButton clearBtn;
    JButton stopBtn;

    //public
    public int instNum = 0;
    public String[][] instructions = {
            { "", "", "", "" },
            { "", "", "", "" },
            { "", "", "", "" },
            { "", "", "", "" },
            { "", "", "", "" },
            { "", "", "", "" },
            { "", "", "", "" },
            { "", "", "", "" },
            { "", "", "", "" },
            { "", "", "", "" }};
	int instTotalNum = 0;
	int [] instPreStatus;
	Object[][] exeStatusData ;
	private String transferResStation(int c){
		switch(c){
			case 0: return "";
			case 1: return "Add1";
			case 2: return "Add2";
			case 3: return "Add3";
			case 4: return "Mul1";
			case 5: return "Mul2";
			case 6: return "Load1";
			case 7: return "Load2";
			case 8: return "Load3";
			case 9: return "Store1";
			case 10: return "Store2";
			case 11: return "Store3";
			default: return "";
		}
	}
	private void updateMe()
	{
		//
		String memContent="";
		for(int i = 0; i< smu.memSize; ++i){
			if(smu.mems.getValue(i*4)!=0){
				memContent=memContent+"["+i*4+"] "+String.valueOf(smu.mems.getValue(i*4))+"\n";
			}
		}
		setMemText(memContent);//所有内存状态
		setTimeLabel(String.valueOf(smu.clock));
		//设定浮点寄存器
		for(int i =0; i<smu.regSize; ++i){
			setFUTable(transferResStation(smu.regs.getQi(i)),0,i);
			setFUTable(String.valueOf(smu.regs.getValue(i)),1,i);
		}
		//设定保留站
		for(int i =0; i<3; ++i){
			System.out.println(smu.adder.getEnd_time() + "-----" + smu.clock);
			if((smu.adder.getRs_id()-1)==i)
				setReservationStationTable(String.valueOf(Math.max((smu.adder.getEnd_time()-smu.clock),0)),i,0);
			else
				setReservationStationTable("0", i, 0);
			setReservationStationTable(smu.addResStation[i].isBusy()?"Busy":"Free",i,2);
			setReservationStationTable(String.valueOf(smu.addResStation[i].getOp()),i,3);
			setReservationStationTable(String.valueOf(smu.addResStation[i].getVj()),i,4);
			setReservationStationTable(String.valueOf(smu.addResStation[i].getVk()),i,5);
			setReservationStationTable(transferResStation(smu.addResStation[i].getQj()),i,6);
			setReservationStationTable(transferResStation(smu.addResStation[i].getQk()),i,7);
		}
		for(int i =0; i<2; ++i){
			int line = i+3;
			if((smu.multiplier.getRs_id()-1)==line)
				setReservationStationTable(String.valueOf(Math.max((smu.multiplier.getEnd_time()-smu.clock),0)),line,0);
			else
				setReservationStationTable("0", line, 0);
			setReservationStationTable(smu.mulResStation[i].isBusy()?"Busy":"Free",line,2);
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getOp()),line,3);
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getVj()),line,4);
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getVk()),line,5);
			setReservationStationTable(transferResStation(smu.mulResStation[i].getQj()),line,6);
			setReservationStationTable(transferResStation(smu.mulResStation[i].getQk()),line,7);
		}
		try{
			//设定LoadQue
			for(int i=0; i<3; ++i){
				setLoadQueueTable(smu.ldResStation[i].isBusy()?"Busy":"Free",i,0);
				setLoadQueueTable(String.valueOf(smu.ldResStation[i].getA()),i,1);
				setLoadQueueTable(String.valueOf(smu.regs.getValue(smu.ldResStation[i].getIns().getRd() ) ),i,2 );
			}
		}catch(Exception e){
			System.out.println(e);
		}
		try{
			for(int i=0; i<3; ++i){
				setStoreQueueTable(smu.stResStation[i].isBusy()?"Busy":"Free",i,0);
				setStoreQueueTable(String.valueOf(smu.stResStation[i].getA()),i,1);
				setStoreQueueTable(String.valueOf(smu.regs.getValue(smu.stResStation[i].getIns().getRd() ) ),i,2 );
			}
		}catch(Exception e){
			System.out.println(e);
		}
		//设定运行状态
		for(int i = 0; i<instTotalNum; ++i){
			if(instPreStatus[i]!=smu.instructionVector.get(i).getState()){
				setExeStatusTable(String.valueOf(smu.clock), i, instPreStatus[i]);
				instPreStatus[i]+=1;
			}
		}
	}

	public MyGUI()
	{
		jf = new JFrame("Tomasulo");
		root = new JPanel();
		BorderLayout borderLayout = new BorderLayout();
		root.setLayout(borderLayout);

		//按钮和其他显示的标签区域
		JPanel showPanel = new JPanel();
		showPanel.setLayout(new BorderLayout());

        JLabel instNumTitleLabel = new JLabel("指令数量：");
        instNumTitleLabel.setBounds(700, 550, 100, 20);
        root.add(instNumTitleLabel);

        instNumLabel = new JTextField("10");
        instNumLabel.setBounds(800, 550, 100, 20);
        root.add(instNumLabel);

		runBtn = new JButton("Run N Steps");
        runBtn.setBounds(950, 550, 100, 20);
        root.add(runBtn);
        //Btn action
        //Get inst num and inst
        runBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
							instNum = Integer.parseInt(instNumLabel.getText());
							for(int i =0 ; i < instNum; ++i){
								smu.step();
								updateMe();
							}

            }
        });
			stepBtn = new JButton("Run 1 Step");
	        stepBtn.setBounds(1050, 550, 100, 20);
	        root.add(stepBtn);
	        stepBtn.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
								smu.step();
								updateMe();
	            }
	        });

        clearBtn = new JButton("Reset");
        clearBtn.setBounds(950, 600, 100, 20);
        root.add(clearBtn);
        //Btn action
        //Get inst num and inst
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
							smu.reset();
            	clearExeStatusTable();
            	clearLoadQueueTable();
            	clearStoreQueueTable();
            	clearMemText();
            	clearReservationStationTable();
            	clearFUTable();
            	clearTimeLabel();
            	clearInstNumLabel();
            }
        });

        stopBtn = new JButton("Load Inst");
        stopBtn.setBounds(1050, 600, 100, 20);
        root.add(stopBtn);
        //Btn action
        //Get inst num and inst
        stopBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
							smu.clearIns();
							smu.readInstruction(instructionText.getText());
							instTotalNum = smu.instructionVector.size();
							instPreStatus = new int[instTotalNum];

            }
        });

        JLabel timeTitleLabel = new JLabel("Time:");
        timeTitleLabel.setBounds(700, 600, 100, 20);
        root.add(timeTitleLabel);

        timeLabel = new JLabel("0");
        timeLabel.setBounds(800, 600, 100, 20);
        root.add(timeLabel);


		//指令
		instructionText = new JTextArea("LD F0 1024\n" +
						"LD F1 1028\n" +
						"MULD F2 F0 F1\n" +
						"SUBD F2 F0 F1\n" +
						"MULD F4 F0 F1\n" +
						"DIVD F5 F0 F1\n" +
						"ST F0 0\n" +
						"ST F1 4\n" +
						"ST F2 8\n" +
						"ST F3 12\n" +
						"ST F4 16\n" +
						"ST F5 20");
		JScrollPane insJSP= new JScrollPane(instructionText);
		insJSP.setBounds(50, 50, 200, 200);
		root.add(insJSP);

        JLabel instListLabel = new JLabel("指令");
        instListLabel.setBounds(70, 30, 100, 20);
        root.add(instListLabel);

        //表格2
				exeStatusData = new Object [40][];
				for(int i =0; i<40; ++i){
					exeStatusData[i] = new Object[4];
					for(int j = 0; j<4; ++j){
						exeStatusData[i][j]="";
					}
				}

        // 创建表格中的横标题
        String[] exeStatusColNames = { "发射指令", "执行完毕", "写回结果" };
        exeStatusTable = new JTable(exeStatusData,exeStatusColNames);
        JScrollPane exeStatusJSP= new JScrollPane(exeStatusTable);
        exeStatusJSP.setBounds(400, 50, 200, 200); ;
        root.add(exeStatusJSP);

        JLabel exeStatusLabel = new JLabel("运行状态");
        exeStatusLabel.setBounds(400, 30, 100, 20);
        root.add(exeStatusLabel);

      //表格3
        Object[][] loadQueueData = {
                // 创建表格中的数据
                { "", "", "" },
                { "", "", "" },
                { "", "", "" },};
        // 创建表格中的横标题
        String[] loadQueueColNames = { "Busy", "Address", "Cache" };
        loadQueueTable = new JTable(loadQueueData,loadQueueColNames);
        JScrollPane loadQueueJSP= new JScrollPane(loadQueueTable);
        loadQueueJSP.setBounds(800, 50, 200, 75);
        root.add(loadQueueJSP);

        JLabel loadQueueLabel = new JLabel("Load Queue");
        loadQueueLabel.setBounds(800, 30, 100, 20);
        root.add(loadQueueLabel);

      //表格4
        Object[][] storeQueueData = {
                // 创建表格中的数据
                { "", "", "" },
                { "", "", "" },
                { "", "", "" },};
        // 创建表格中的横标题
        String[] storeQueueColNames = { "Busy", "Address", "Cache" };
        storeQueueTable = new JTable(storeQueueData,storeQueueColNames);
        JScrollPane storeQueueJSP= new JScrollPane(storeQueueTable);
        storeQueueJSP.setBounds(800, 150, 200, 75); ;
        root.add(storeQueueJSP);

        JLabel storeQueueLabel = new JLabel("Store Queue");
        storeQueueLabel.setBounds(800, 130, 100, 20);
        root.add(storeQueueLabel);

      //表格5
			memText = new JTextArea();
        JScrollPane memJSP= new JScrollPane(memText);
        memJSP.setBounds(50, 300, 200, 200);
        root.add(memJSP);

        JLabel memLabel = new JLabel("内存");
        memLabel.setBounds(50, 270, 100, 20);
        root.add(memLabel);

      //表格6
        Object[][] reservationStationData = {
                // 创建表格中的数据
                { "", "Add1", "", "", "","","",""},
                { "", "Add2", "", "", "","","",""},
								{ "", "Add3", "", "", "","","",""},
                { "", "Mul1", "", "", "","","",""},
                { "", "Mul2", "", "", "","","",""},};
        // 创建表格中的横标题
        String[] reservationStationColNames = { "Time","Name","Busy", "Op", "Vi","Vk","Qi","Qk" };
        reservationStationTable = new JTable(reservationStationData,reservationStationColNames);
        JScrollPane reservationStationJSP= new JScrollPane(reservationStationTable);
        reservationStationJSP.setBounds(400, 370, 500, 110);
        root.add(reservationStationJSP);

        JLabel reservationStationLabel = new JLabel("保留站");
        reservationStationLabel.setBounds(400, 350, 100, 20);
        root.add(reservationStationLabel);
				JTextArea inst = new JTextArea("请在修改指令后 或 执行前先点击Load Inst按钮以读取指令\n\nRun N Steps根据左边的文本框中数字执行指令条数");
				// JLabel inst = new JLabel("请在修改指令后 或 执行前先</br>点击Load Inst按钮以读取指令</br></br>Run N Steps根据左边的文本框中</br>数字执行指令条数");
				inst.setLineWrap(true);
				inst.setWrapStyleWord(true);
				inst.setEditable(false);
        inst.setBounds(950, 370, 200, 100);
				inst.setBackground(new Color(238,238,238));
        root.add(inst);
      //表格7
        Object[][] FUData = {
                // 创建表格中的数据
                { "", "", "", "", "","","","","","","",""},
                { "", "", "", "", "","","","","","","",""},
                };
        // 创建表格中的横标题
        String[] FUColNames = { "F0","F1","F2", "F3", "F4","F5","F6","F7","F8","F9","F10" };
        FUTable = new JTable(FUData,FUColNames);
        JScrollPane FUJSP= new JScrollPane(FUTable);
        FUJSP.setBounds(50, 550, 600, 80);
        root.add(FUJSP);

        JLabel FULabel = new JLabel("浮点寄存器FU");
        FULabel.setBounds(50, 530, 100, 20);
        root.add(FULabel);

        JLabel tmpLabel = new JLabel(" ");
        tmpLabel.setBounds(50, 580, 100, 20);
        root.add(tmpLabel);


    	 jf.setContentPane(root);
	     jf.setSize(WIDTH, HEIGHT);
	     jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	     jf.setVisible(true);
	}

	//设置状态表的内容
	public void setExeStatusTable(String value, int i, int j)
	{
		exeStatusTable.setValueAt(value, i, j);
	}
	//设置load table表的内容/
	public void setLoadQueueTable(String value, int i, int j)
	{
		loadQueueTable.setValueAt(value, i, j);
	}
	//设置store table表的内容
	public void setStoreQueueTable(String value, int i, int j)
	{
		storeQueueTable.setValueAt(value, i, j);
	}
	//设置Mem表的内容/
	public void setMemText(String value)
	{
		memText.setText(value);
	}
	//设置保留站表的内容
	public void setReservationStationTable(String value, int i, int j)
	{
		reservationStationTable.setValueAt(value, i, j);
	}
	//设置FU表的内容/
	public void setFUTable(String value, int i, int j)
	{
		FUTable.setValueAt(value, i, j);
	}
	//设置RU表的内容
	// public void setRUTable(String value, int i, int j)
	// {
	// 	RUTable.setValueAt(value, i, j);
	// }

	// //设置PC label的内容/
	// public void setPCLabel(String value)
	// {
	// 	PCLabel.setText(value);
	// }
	//设置PC label的内容/
	public void setTimeLabel(String value)
	{
		timeLabel.setText(value);
	}

	//clear
	//clear状态表的内容
		public void clearInstListTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // 创建表格中的数据
	                { "", "", "", "" },
	                { "", "", "", "" },
	                { "", "", "", "" },
	                { "", "", "", "" },
	                { "", "", "", "" },
	                { "", "", "", "" },
	                { "", "", "", "" },
	                { "", "", "", "" },
	                { "", "", "", "" },
	                { "", "", "", "" },};
	        // 创建表格中的横标题
	        String[] columns = { "Name", "Destination", "Sourcej", "Sourcek" };
	        tableModel.setDataVector(data, columns);
	        instListTable.setModel(tableModel);
		}
		public void clearExeStatusTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			for(int i =0; i<40; ++i){
				for(int j = 0; j<4; ++j){
					exeStatusData[i][j]="";
				}
			}
	        // 创建表格中的横标题
	        String[] columns = { "发射指令", "执行完毕", "写回结果" };
	        tableModel.setDataVector(exeStatusData, columns);
	        exeStatusTable.setModel(tableModel);
		}
		public void clearLoadQueueTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // 创建表格中的数据
					{ "", "", "" },
	                { "", "", "" },
	                { "", "", "" },};
	        // 创建表格中的横标题
	        String[] columns = { "Busy", "Address", "Cache" };
	        tableModel.setDataVector(data, columns);
	        loadQueueTable.setModel(tableModel);
		}
		public void clearStoreQueueTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // 创建表格中的数据
					{ "", "", "" },
	                { "", "", "" },
	                { "", "", "" },};
	        // 创建表格中的横标题
	        String[] columns = { "Busy", "Address", "Cache" };
	        tableModel.setDataVector(data, columns);
	        storeQueueTable.setModel(tableModel);
		}
		public void clearMemText()
		{
			memText.setText("");
		}
		//设置保留站表的内容
		public void clearReservationStationTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // 创建表格中的数据
					{ "", "Add1", "", "", "","","",""},
          { "", "Add2", "", "", "","","",""},
					{ "", "Add3", "", "", "","","",""},
          { "", "Mul1", "", "", "","","",""},
          { "", "Mul2", "", "", "","","",""},};
	        // 创建表格中的横标题
	        String[] columns = { "Time","Name","Busy", "Op", "Vj","Vk","Qj","Qk" };
	        tableModel.setDataVector(data, columns);
	        reservationStationTable.setModel(tableModel);
		}
		//设置FU表的内容
		public void clearFUTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // 创建表格中的数据
					{ "", "", "", "", "","","","","","","",""},
	                { "", "", "", "", "","","","","","","",""},};
	        // 创建表格中的横标题
	        String[] columns = { "F0","F1","F2", "F3", "F4","F5","F6","F7","F8","F9","F10"};
	        tableModel.setDataVector(data, columns);
	        FUTable.setModel(tableModel);
		}
		public void clearTimeLabel()
		{
			timeLabel.setText("0");
		}
		public void clearInstNumLabel()
		{
			instNumLabel.setText("10");
		}
		public void clearMiddle(){
			instTotalNum = 0;
		}
		public static void main(String[] args) {
			MyGUI mygui = new MyGUI();
			smu = new Simulator();
			smu.writeMemory(1024, 4);
			smu.writeMemory(1028, 8);
			// smu.run(false, 0);
		}

}
