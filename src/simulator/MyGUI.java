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
	private void updateMe()
	{
		//
		String memContent="";
		for(int i = 0; i< smu.memSize; ++i){
			if(smu.mems.getValue(i*4)!=0){
				memContent=memContent+"["+i*4+"] "+String.valueOf(smu.mems.getValue(i*4))+"\n";
			}
		}
		setMemText(memContent);//�����ڴ�״̬
		setTimeLabel(String.valueOf(smu.clock));
		//�趨����Ĵ���
		for(int i =0; i<smu.regSize; ++i){
			setFUTable(String.valueOf(smu.regs.getQi(i)),0,i);
			setFUTable(String.valueOf(smu.regs.getValue(i)),1,i);
		}
		//�趨����վ
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
			setReservationStationTable(String.valueOf(smu.addResStation[i].getQj()),i,6);
			setReservationStationTable(String.valueOf(smu.addResStation[i].getQk()),i,7);
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
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getQj()),line,6);
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getQk()),line,7);
		}
		try{
			//�趨LoadQue
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
		//�趨����״̬
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

		//��ť��������ʾ�ı�ǩ����
		JPanel showPanel = new JPanel();
		showPanel.setLayout(new BorderLayout());

        JLabel instNumTitleLabel = new JLabel("ָ��������");
        instNumTitleLabel.setBounds(700, 500, 100, 20);
        root.add(instNumTitleLabel);

        instNumLabel = new JTextField("10");
        instNumLabel.setBounds(800, 500, 100, 20);
        root.add(instNumLabel);

        JLabel PCTitleLabel = new JLabel("PC:");
        PCTitleLabel.setBounds(700, 550, 100, 20);
        root.add(PCTitleLabel);

		runBtn = new JButton("Run N Step");
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


		//ָ��
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
		insJSP.setBounds(50, 50, 200, 150);
		root.add(insJSP);

        JLabel instListLabel = new JLabel("ָ��");
        instListLabel.setBounds(70, 30, 100, 20);
        root.add(instListLabel);

        //���2
				exeStatusData = new Object [40][];
				for(int i =0; i<40; ++i){
					exeStatusData[i] = new Object[4];
					for(int j = 0; j<4; ++j){
						exeStatusData[i][j]="";
					}
				}

        // ��������еĺ����
        String[] exeStatusColNames = { "����ָ��", "ִ�����", "д�ؽ��" };
        exeStatusTable = new JTable(exeStatusData,exeStatusColNames);
        JScrollPane exeStatusJSP= new JScrollPane(exeStatusTable);
        exeStatusJSP.setBounds(400, 50, 200, 150); ;
        root.add(exeStatusJSP);

        JLabel exeStatusLabel = new JLabel("����״̬");
        exeStatusLabel.setBounds(400, 30, 100, 20);
        root.add(exeStatusLabel);

      //���3
        Object[][] loadQueueData = {
                // ��������е�����
                { "", "", "" },
                { "", "", "" },
                { "", "", "" },};
        // ��������еĺ����
        String[] loadQueueColNames = { "Busy", "Address", "Cache" };
        loadQueueTable = new JTable(loadQueueData,loadQueueColNames);
        JScrollPane loadQueueJSP= new JScrollPane(loadQueueTable);
        loadQueueJSP.setBounds(800, 50, 200, 75);
        root.add(loadQueueJSP);

        JLabel loadQueueLabel = new JLabel("Load Queue");
        loadQueueLabel.setBounds(800, 30, 100, 20);
        root.add(loadQueueLabel);

      //���4
        Object[][] storeQueueData = {
                // ��������е�����
                { "", "", "" },
                { "", "", "" },
                { "", "", "" },};
        // ��������еĺ����
        String[] storeQueueColNames = { "Busy", "Address", "Cache" };
        storeQueueTable = new JTable(storeQueueData,storeQueueColNames);
        JScrollPane storeQueueJSP= new JScrollPane(storeQueueTable);
        storeQueueJSP.setBounds(800, 150, 200, 75); ;
        root.add(storeQueueJSP);

        JLabel storeQueueLabel = new JLabel("Store Queue");
        storeQueueLabel.setBounds(800, 130, 100, 20);
        root.add(storeQueueLabel);

      //���5
			memText = new JTextArea();
        JScrollPane memJSP= new JScrollPane(memText);
        memJSP.setBounds(50, 300, 200, 75);
        root.add(memJSP);

        JLabel memLabel = new JLabel("�ڴ�");
        memLabel.setBounds(50, 280, 100, 20);
        root.add(memLabel);

      //���6
        Object[][] reservationStationData = {
                // ��������е�����
                { "", "Add1", "", "", "","","",""},
                { "", "Add2", "", "", "","","",""},
								{ "", "Add3", "", "", "","","",""},
                { "", "Mul1", "", "", "","","",""},
                { "", "Mul2", "", "", "","","",""},};
        // ��������еĺ����
        String[] reservationStationColNames = { "Time","Name","Busy", "Op", "Vi","Vk","Qi","Qk" };
        reservationStationTable = new JTable(reservationStationData,reservationStationColNames);
        JScrollPane reservationStationJSP= new JScrollPane(reservationStationTable);
        reservationStationJSP.setBounds(400, 300, 500, 116);
        root.add(reservationStationJSP);

        JLabel reservationStationLabel = new JLabel("����վ");
        reservationStationLabel.setBounds(450, 280, 100, 20);
        root.add(reservationStationLabel);

      //���7
        Object[][] FUData = {
                // ��������е�����
                { "", "", "", "", "","","","","","","",""},
                { "", "", "", "", "","","","","","","",""},
                };
        // ��������еĺ����
        String[] FUColNames = { "F0","F1","F2", "F3", "F4","F5","F6","F7","F8","F9","F10" };
        FUTable = new JTable(FUData,FUColNames);
        JScrollPane FUJSP= new JScrollPane(FUTable);
        FUJSP.setBounds(50, 500, 600, 80);
        root.add(FUJSP);

        JLabel FULabel = new JLabel("����Ĵ���FU");
        FULabel.setBounds(50, 480, 100, 20);
        root.add(FULabel);

        JLabel tmpLabel = new JLabel(" ");
        tmpLabel.setBounds(50, 580, 100, 20);
        root.add(tmpLabel);


    	 jf.setContentPane(root);
	     jf.setSize(WIDTH, HEIGHT);
	     jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	     jf.setVisible(true);
	}

	//����״̬�������
	public void setExeStatusTable(String value, int i, int j)
	{
		exeStatusTable.setValueAt(value, i, j);
	}
	//����load table�������/
	public void setLoadQueueTable(String value, int i, int j)
	{
		loadQueueTable.setValueAt(value, i, j);
	}
	//����store table�������
	public void setStoreQueueTable(String value, int i, int j)
	{
		storeQueueTable.setValueAt(value, i, j);
	}
	//����Mem�������/
	public void setMemText(String value)
	{
		memText.setText(value);
	}
	//���ñ���վ�������
	public void setReservationStationTable(String value, int i, int j)
	{
		reservationStationTable.setValueAt(value, i, j);
	}
	//����FU�������/
	public void setFUTable(String value, int i, int j)
	{
		FUTable.setValueAt(value, i, j);
	}
	//����RU�������
	// public void setRUTable(String value, int i, int j)
	// {
	// 	RUTable.setValueAt(value, i, j);
	// }

	// //����PC label������/
	// public void setPCLabel(String value)
	// {
	// 	PCLabel.setText(value);
	// }
	//����PC label������/
	public void setTimeLabel(String value)
	{
		timeLabel.setText(value);
	}

	//clear
	//clear״̬�������
		public void clearInstListTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // ��������е�����
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
	        // ��������еĺ����
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
	        // ��������еĺ����
	        String[] columns = { "����ָ��", "ִ�����", "д�ؽ��" };
	        tableModel.setDataVector(exeStatusData, columns);
	        exeStatusTable.setModel(tableModel);
		}
		public void clearLoadQueueTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // ��������е�����
					{ "", "", "" },
	                { "", "", "" },
	                { "", "", "" },};
	        // ��������еĺ����
	        String[] columns = { "Busy", "Address", "Cache" };
	        tableModel.setDataVector(data, columns);
	        loadQueueTable.setModel(tableModel);
		}
		public void clearStoreQueueTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // ��������е�����
					{ "", "", "" },
	                { "", "", "" },
	                { "", "", "" },};
	        // ��������еĺ����
	        String[] columns = { "Busy", "Address", "Cache" };
	        tableModel.setDataVector(data, columns);
	        storeQueueTable.setModel(tableModel);
		}
		public void clearMemText()
		{
			memText.setText("");
		}
		//���ñ���վ�������
		public void clearReservationStationTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // ��������е�����
					{ "", "Add1", "", "", "","","",""},
          { "", "Add2", "", "", "","","",""},
					{ "", "Add3", "", "", "","","",""},
          { "", "Mul1", "", "", "","","",""},
          { "", "Mul2", "", "", "","","",""},};
	        // ��������еĺ����
	        String[] columns = { "Time","Name","Busy", "Op", "Vj","Vk","Qj","Qk" };
	        tableModel.setDataVector(data, columns);
	        reservationStationTable.setModel(tableModel);
		}
		//����FU�������
		public void clearFUTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // ��������е�����
					{ "", "", "", "", "","","","","","","",""},
	                { "", "", "", "", "","","","","","","",""},};
	        // ��������еĺ����
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
