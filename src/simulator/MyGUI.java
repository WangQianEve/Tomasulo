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
	private void updateMe()
	{
		String memContent="";
		for(int i = 0; i< smu.memSize; ++i){
			if(smu.mems.getValue(i)!=0){
				memContent=memContent+"["+i+"] "+String.valueOf(smu.mems.getValue(i))+"\n";
			}
		}
		setMemText(memContent);//�����ڴ�״̬
		setTimeLabel(String.valueOf(smu.clock));
		for(int i =0; i<smu.regSize; ++i){
			setFUTable(String.valueOf(smu.regs.getQi(i)),0,i);
			setFUTable(String.valueOf(smu.regs.getValue(i)),1,i);
		}
		for(int i =0; i<3; ++i){
			if((smu.adder.rs_id-1)==i)
				setReservationStationTable(String.valueOf(smu.adder.end_time-smu.clock),i,0);
			setReservationStationTable(smu.addResStation[i].isBusy().toString(),i,2);
			setReservationStationTable(String.valueOf(smu.addResStation[i].getOp()),i,3);
			setReservationStationTable(String.valueOf(smu.addResStation[i].getVj()),i,4);
			setReservationStationTable(String.valueOf(smu.addResStation[i].getVk()),i,5);
			setReservationStationTable(String.valueOf(smu.addResStation[i].getQj()),i,6);
			setReservationStationTable(String.valueOf(smu.addResStation[i].getQk()),i,7);
		}
		for(int i =0; i<2; ++i){
			int line = i+3;
			if((smu.multiplier.rs_id-1)==i)
			  setReservationStationTable(String.valueOf(smu.multiplier.end_time-smu.clock),i,0);
			setReservationStationTable(smu.mulResStation[i].isBusy().toString(),i,2);
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getOp()),i,3);
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getVj()),i,4);
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getVk()),i,5);
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getQj()),i,6);
			setReservationStationTable(String.valueOf(smu.mulResStation[i].getQk()),i,7);
		}
		for(int i=0; i<3; ++i){
			setLoadQueueTable(smu.ldResStation[i].isBusy().toString(),i,0);
			setLoadQueueTable(String.valueOf(smu.ldResStation[i].getA()),i,1);
			setLoadQueueTable(String.valueOf(smu.regs.get(smu.ldResStation[i].getIns().getRd() ) ),i,2 );
		}
		for(int i=0; i<3; ++i){
			setLoadQueueTable(smu.stResStation[i].isBusy().toString(),i,0);
			setLoadQueueTable(String.valueOf(smu.stResStation[i].getA()),i,1);
			setLoadQueueTable(String.valueOf(smu.regs.get(smu.stResStation[i].getIns().getRd() ) ),i,2 );
		}
		for(int i = 0; i<instTotalNum; ++i){
			if(instPreStatus[i]!=smu.instructionVector.get(i).getState()){
				setExeStatusTable(String.valueOf(smu.clock),i,instPreStatus[i]);
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

        instNumLabel = new JTextField("0");
        instNumLabel.setBounds(800, 500, 100, 20);
        root.add(instNumLabel);

        JLabel PCTitleLabel = new JLabel("PC:");
        PCTitleLabel.setBounds(700, 550, 100, 20);
        root.add(PCTitleLabel);

        PCLabel = new JLabel("0");
        PCLabel.setBounds(800, 550, 100, 20);
        root.add(PCLabel);

		runBtn = new JButton("Run N Step");
        runBtn.setBounds(950, 550, 100, 20);
        root.add(runBtn);
        //Btn action
        //Get inst num and inst
        runBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
							instNum = Integer.parseInt(instNumLabel.getText());
							for(int i =0 ; i< instNum; ++i){
								smu.step();
							}
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
            	clearExeStatusTable();
            	clearLoadQueueTable();
            	clearStoreQueueTable();
            	clearMemTable();
            	clearReservationStationTable();
            	clearFUTable();
            	clearRUTable();
            	clearPCLabel();
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
							System.out.println(instructionText.getText());
							smu.readInstruction(instructionText.getText());
							instTotalNum = smu.instructionVector.size();
							instPreStatus = new int[instTotalNum];
							smu.reset();
            }
        });

        JLabel timeTitleLabel = new JLabel("Time:");
        timeTitleLabel.setBounds(700, 600, 100, 20);
        root.add(timeTitleLabel);

        timeLabel = new JLabel("0");
        timeLabel.setBounds(800, 600, 100, 20);
        root.add(timeLabel);


		//ָ��
		instructionText = new JTextArea();
		JScrollPane insJSP= new JScrollPane(instructionText);
		insJSP.setBounds(50, 50, 200, 150);
		root.add(insJSP);

        JLabel instListLabel = new JLabel("ָ��");
        instListLabel.setBounds(70, 30, 100, 20);
        root.add(instListLabel);

        //���2
        Object[][] exeStatusData ;
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
                { "", "", "", "", "","","",""},
                { "", "", "", "", "","","","" },
                { "", "", "", "", "","","","" },};
        // ��������еĺ����
        String[] reservationStationColNames = { "Time","Name","Busy", "Op", "Vi","Vk","Qi","Qk" };
        reservationStationTable = new JTable(reservationStationData,reservationStationColNames);
        JScrollPane reservationStationJSP= new JScrollPane(reservationStationTable);
        reservationStationJSP.setBounds(400, 300, 500, 100);
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

      //���8
        Object[][] RUData = {
                // ��������е�����
                { "", "", "", "", "","","","","","","",""},
                { "", "", "", "", "","","","","","","",""},
                };
        // ��������еĺ����
        String[] RUColNames = { "F0","F1","F2", "F3", "F4","F5","F6","F7","F8","F9","F10" };
        RUTable = new JTable(RUData,RUColNames);
        JScrollPane RUJSP= new JScrollPane(RUTable);
        RUJSP.setBounds(50, 600, 600, 80);
        root.add(RUJSP);

        JLabel RULabel = new JLabel("���ͼĴ���RU");
        RULabel.setBounds(50, 580, 100, 20);
        root.add(RULabel);

        JLabel tmpLabel = new JLabel(" ");
        tmpLabel.setBounds(50, 580, 100, 20);
        root.add(tmpLabel);


	    //jf.add(new JScrollPane(root));
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
	        String[] columns = { "����ָ��", "ִ�����", "д�ؽ��" };
	        tableModel.setDataVector(data, columns);
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
		public void clearMemTable()
		{
			memText.setText("");
		}
		//���ñ���վ�������
		public void clearReservationStationTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // ��������е�����
					{ "", "", "", "", "","","",""},
	                { "", "", "", "", "","","","" },
	                { "", "", "", "", "","","","" },};
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
		//����RU�������
		public void clearRUTable()
		{
			DefaultTableModel tableModel = new DefaultTableModel();
			Object[][] data = {
	                // ��������е�����
					{ "", "", "", "", "","","","","","","",""},
	                { "", "", "", "", "","","","","","","",""},};
	        // ��������еĺ����
	        String[] columns = { "F0","F1","F2", "F3", "F4","F5","F6","F7","F8","F9","F10"};
	        tableModel.setDataVector(data, columns);
	        RUTable.setModel(tableModel);
		}

		//����PC label������
		public void clearPCLabel()
		{
			PCLabel.setText("0");
		}
		public void clearTimeLabel()
		{
			timeLabel.setText("0");
		}
		public void clearInstNumLabel()
		{
			instNumLabel.setText("0");
		}

		public static void main(String[] args) {
			MyGUI mygui = new MyGUI();
			smu = new Simulator();
			smu.writeMemory(1024, 4);
			smu.writeMemory(1028, 8);
			// smu.run(false, 0);
		}

}
