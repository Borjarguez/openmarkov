header {
    package org.openmarkov.learning.io;
    import org.apache.log4j.Logger;
	import org.openmarkov.core.model.network.NodeType;
	import org.openmarkov.core.model.network.ProbNet;
	import org.openmarkov.core.model.network.ProbNode;
	import org.openmarkov.core.model.network.State;
	import org.openmarkov.core.model.network.Variable;
	import org.openmarkov.core.model.network.type.BayesianNetworkType;
    import java.util.*;
}

class ArffLexer extends Lexer;

options {filter=true;
testLiterals=false;    // don't automatically test for literals
k=6;}

QUOTATION : ('"'|'\''); 

COMMA	: ',';

LEFTB : '{';

RIGHTB : '}';

WHITE	: (' '|'\t')
		{ $setType(Token.SKIP); };

REMARK :	( '%' (~('\n'|'\r'))* ('\n'|'\r'('\n')?) )
		{  $setType(Token.SKIP); newline(); };

SEPARATOR : ('\n' | '\r'('\n')?)
		{  $setType(Token.SKIP); newline(); };

IDENT
	options {testLiterals=true;}
	:	('a'..'z'|'A'..'Z'|'_'|'$'|'0'..'9')
 ('a'..'z'|'\u00E1'|'\u00E9'|'\u00ED'|'\u00F3'|'\u00FA'|'\u00FC'|'A'..'Z'|'\u00C1'|'\u00C9'|'\u00CD'|'\u00D3'|'\u00DA'|'\u00DC'|'_'|'$'|'&'|'-'|'0'..'9'|'%'|'.')*;

STRING
	options {testLiterals=true;}
	: QUOTATION! ('a'..'z'|'\u00E1'|'\u00E9'|'\u00ED'|'\u00F3'|'\u00FA'|'\u00FC'|'A'..'Z'|'\u00C1'|'\u00C9'|'\u00CD'|'\u00D3'|'\u00DA'|'\u00DC'|'_'|'$'|'&'|'-'|'0'..'9'|
	'.'|'%'|'>'|'<'|'='|'/'|'|'|'\\'|'?'|','|' '|'('|')'|':'|'\r'('\n')?)* 
		QUOTATION!;

RELATION : ("@relation" | "@RELATION"); 

ATTRIBUTE : ("@attribute" | "@ATTRIBUTE");

DATA : ("@data" | "@DATA");

class ArffParser extends Parser;

options {
	buildAST = true;
	k = 7;
}

{
	/** Global variable */
	private ArrayList<Variable> fsVariables = new ArrayList<Variable>();

	/** Global variable to store the variables while they are reading from
	 * an input file */
	private ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());

	private ProbNet bayesNet = new ProbNet(BayesianNetworkType.getUniqueInstance());

	/** Stores information about Elvira nets */
	private HashMap<String, Object> ioNet = new HashMap<String, Object>();
	
	private int casesCont = 0;
	
	private int[][] cases;
	
	private ArrayList<int[]> casesAux; //we know the number of cases while reading
	
	private int[] example;

	private ArrayList<String> statesVar;
	
	private int index=0;
	
	/** Importance of messages generated by the parser. Initially the minimum */
	private int messagesImportance = 2;

	/** Default states. Global variable */
	private String[] defaultStates;
	
	/** A utility node has no states, for computational reasons we define a
      * set of one state */
	private String[] defaultUtilityStates={""};
	
	/** Messages file */
	private Logger logFile = Logger.getLogger (ArffParser.class);
	
	// Chance = Potential; Utility = utility
	private NodeType kindRelation = NodeType.CHANCE;  
	
    /** Adds a <code>ProbNode</code> to the <code>ProbNet</code>. */
	public ProbNode addProbNode(HashMap<String, String> infoNode, State[] states,
			NodeType nodeType, String nodeName) {

		Variable fsVariable = null;
		String statesSource;
		int numStates;
		
		if (states.length != 0) {
			fsVariable = new Variable(nodeName, states);
		}
		ProbNode probNode = null;
		try {
			probNode = probNet.addVariable(fsVariable, nodeType);
			probNode.properties = infoNode;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return probNode;
	}
	
	/** Sets the importance of messages generated by the parser
	 * @param importance */
	public void setMessagesImportance(int importance) {
		messagesImportance = importance;
	}
	
	public ProbNet getProbNet(){
		return probNet;
	}
	
	public int[][] getCases(){
		return cases;
	}
}

relation returns [HashMap<String, Object> bn=null]
{
	String name = null;
	double visualprecisionNet = Double.MIN_VALUE, versionNet = Double.MIN_VALUE;
	
    probNet = bayesNet;
}
 : b:RELATION^ (s1:IDENT | s2:STRING) attributes data {
	
	if(s1!=null)
		name = s1.getText();
	else
		name = s2.getText();
    ioNet.put("Name", name);
    
	logFile.debug("Bayes net name: " + name);
	logFile.debug("--------------------------");
	ioNet.put("ProbNet", probNet);
	bn = ioNet; 
};

attributes : {
	fsVariables = new ArrayList<Variable>();
} (attribute)+ {
	int numVariables = probNet.getChanceAndDecisionVariables().size();

	casesAux = new ArrayList<int[]>();
	example = new int[numVariables];
	
        for (int i=0; i<numVariables; i++)
            fsVariables.add(probNet.getChanceAndDecisionVariables().get(i));
	
	Variable fsVariable;
	for (int i = numVariables-1; i >= 0; i--) {
		fsVariable = fsVariables.get(i);
    	State[] states = fsVariable.getStates();
	    logFile.debug("Variable " + fsVariable.getName() + " states: ");
    	
    	for (int j = 0; j < states.length; j++) {
    		logFile.debug(states[j] + " ");
    	}
		logFile.debug("");
	
/*	    try {
    	    probNet.addVariable(fsVariables.get(i), NodeType.CHANCE);
    	} catch (Exception e) {
    		System.err.println("Problems creating BayesNet");
		    System.err.println(e.getMessage());
	    }		*/
	}
};

attribute { HashMap<String, String> infoNode = null; 
		statesVar = new ArrayList<String>();}:
         n:ATTRIBUTE^ (id1:IDENT! | id2:STRING!) states {

	NodeType typeOfNode=NodeType.CHANCE;
	infoNode = new HashMap<String, String>();
	ProbNode probNode;
	State[] states = new State[statesVar.size()];
	int i=0;
	for(String state : statesVar){
		states[i] = new State(statesVar.get(i));
		i++;
	} 
	if (id1 != null)
		infoNode.put("Name", id1.getText());
	else
		infoNode.put("Name", id2.getText());
	infoNode.put("UseDefaultStates", "false");
	infoNode.put("CoordinateX", "0");
	infoNode.put("CoordinateY", "0");
	if(id1 != null)
		probNode = addProbNode(infoNode, states, typeOfNode, 
			id1.getText());
	else
		probNode = addProbNode(infoNode, states, typeOfNode,
			id2.getText());

    logFile.debug(n.getText());

//	Variable fsVariable = null;
    if (infoNode != null) {
//    	fsVariable = new Variable(id.getText(), infoNode.getStates());
    	if (infoNode.get("NodeStates") != null) {
	    	logFile.debug(". Num states: " + states.length);
    	}
    } else {
//    	fsVariable = new Variable(id.getText(),defaultStates);
	    logFile.debug(". Num states: "+defaultStates.length);
    } 
//    fsVariables.add(fsVariable);
	probNode.properties = infoNode;
};

states : lb:LEFTB! state! (c:COMMA! state!)* rb:RIGHTB! {};

state : (s:STRING | i:IDENT) {
	if (s==null)
		statesVar.add(i.getText());
	else
		statesVar.add(s.getText());
};

data : DATA! (example)* {	
	cases = new int[casesAux.size()][probNet.getChanceAndDecisionVariables().size()];
	for (int i=0; i< casesAux.size(); i++)
		cases[i] = casesAux.get(i);
};

example : (caseData COMMA!)* caseData {
	index=0;
	casesAux.add(example);
	casesCont++;
	example = new int[probNet.getChanceAndDecisionVariables().size()];
};

caseData returns [int result=0] : (l1:IDENT | l2:STRING) {
	try{
		if (l1!=null)
   			example[index] = fsVariables.get(index).getStateIndex(l1.getText());
   		else
   			example[index] = fsVariables.get(index).getStateIndex(l2.getText());
   		index++;
	}catch(Exception e){}
};


