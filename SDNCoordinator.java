package com.TCC2.sistema;

import grafo.Edge;



import grafo.Node;
import grafo.Topology;

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import routing.GenerateRoute;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.Checksum;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetExplicitTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest.Option;
import com.rapplogic.xbee.util.DoubleByte;


public class SDNCoordinator{
	
	
	
	Hashtable Addresses = new Hashtable(); //Mapeia Enderecos de 64 bits em 16 bits
	Hashtable Neighbors = new Hashtable(); //objeto da classe da Estrutura de dados    ï¿½ a estrutura de dados para a tabea de vizinhos
	Hashtable TableRoute = new Hashtable(); //Tebela de rotas
	
	
	
	
	
	
	
    private String solicitante;    
    private String address64;
    private int msb16;
    private int lsb16;
    private int[] msbVizinho16 = new int[30];
    private int[] lsbVizinho16 = new int[30];    
    private String vizinho16;
    private String vizinho64;
    private int[] endereco = new int[8];
    private int[] lqi = new int[30];
    private int somaSolicitante64;
    private int somaSolicitante16;
    private int[] somaVizinho16 = new int[30];
    private int[] somaVizinho64 = new int[30];
    int somaRota = 0;
    
    int roteadorDADOS;
    
	String RouteAddress16;
	String RouteAddress64;
	int RouteAdd64;			//somatório do 5º e 7º valor do endereco de 64 bits	
	int[] rotas;
	int cont = 0;
	String endereco64;
	String endereco16;
    
	int destinosMSB[] = new int[10];
	int destinosLSB[] = new int[10];
    
    
    String[] n = new String[3];
    int cs = 0;
    String pacote;
    String[] split;
    String dados;
    String[] d;
    String packet;
    String[] ack;			
	
	String tabela;		
	String[] teste;
    
    
    
    private String data;
    int contador = 0;
    //Node nodes[] = new Node[30];  
    List<Node> nodes;
    List<String> roteamento;
    
    
    Node node;
    Node coordenator;
    Node r;
    Node c;
    
    
    String neighbors = null;
    
    Endereco calcula = new Endereco(); 
    
    XBee xbee = new XBee();
    
    XBeeResponse frame;   
    
    Topology topo = new Topology();    //CRIO O OBJETO DA TOPOLOGIA VIRTUAL
    
    
	public String getSolicitante() {
		return solicitante;
	}

	public void setSolicitante(String solicitante) {
		this.solicitante = solicitante;
	}

	
	File file = new File("C:/Users/Alex/workspace2/logs.txt"); //LOCAL ONDE ESTARÁ O ARQUIVO DE LOG
	
	FileWriter fw = new FileWriter(file.getAbsoluteFile());
	BufferedWriter bw = new BufferedWriter(fw);
	
	
	
	private final static Logger log = Logger.getLogger(SDNCoordinator.class);	
	
	private void RouteCalculate(int rota) throws Exception{				
		
		
				
		//dest16 = 100;	
		
		List<Node> teste = topo.getNodeList();	
		
		//for (Node node : teste){
		//	System.out.println("NODO: "+node.getAddress16());
		//}
		
		//PRIMEIRAMENTE REALIZO UMA VERIFICAÇÃO DOS NÓS NA TOPOLOGIA VIRTUAL
		for(Node node : teste){			
			List<Edge> v = node.getNeighbors();
			System.out.println("NODO: "+node.getAddress16());
			for(Edge n : v){
				System.out.println("Vizinho: "+n.getNode().getAddress16()+"LQI: "+n.getWeight());
			}
		}
		
		
		System.out.println("ROTA: "+rota);
		
		//Thread.sleep(100000);		
		
		GenerateRoute route = new GenerateRoute();
		
		//CHAMO O MÉTODO DA CLASSE GENERATEROUTE PARA CALCULAR A MELHOR ROTA PARA AQUELE DETERMINADO DESTINO		
		List<Integer> sp = route.requestShortestPath(topo, somaSolicitante16, rota);
				
		int numAddrInteger = 0;
		String routing = null;
		
		//RECOLHO AS "INFORMAÇÕES ENDEREÇOS" DE 16 BITS DE SALTOS ATÉ O DESTINO
		for (int i = 0; i < sp.size(); i++) {
			System.out.println("No "+i+": "+sp.get(sp.size()-i-1));
			routing = ""+ sp.get(sp.size()-i-1);
			routing += ",";
			numAddrInteger = i; //O NÚMERO DE SALTOS VAI SER EQUIVALENTE AO NÚMERO DE INTERAÇÕES DO LAÇO
		}
		
		String numAddreses = ""+ numAddrInteger; //CONVERTO A VARIÁVEL INT PARA STRING
		
		String rot = numAddreses + "/" + routing; //[NumAddreses/Rota1,Rota2...] //ESTRUTURA COM OS NUMEROS DE ENDEREÇOS DE SALTOS E AS ROTAS
		
		TableRoute.put(somaSolicitante16,rot);	//ARMAZENO AS INFORMAÇÕES EM ESTRUTURA DE DADOS ESPECÍFICA PARA AS ROTAS CALCULADAS
		
		String tipo = "ROUTING";	//ESPECIFICO O TIPO DE MENSAGEM A SER ENVIADA
		
		SendData(tipo);	//PASSO POR PARÂMETRO AS INFORMAÇÕES CALCULADAS PARA SEREM ENVIADAS AO DETERMINADO ROTEADOR SOLICITANTE
		
		
	}
	
		
private void ProcessVizinhosRouters() throws Exception{	
		//APÓS O COORDENADOR CRIAR UMA COMUNICAÇÃO COM OS ROTEADORES ATRAVÉS DO CREATE SOURCE ROUTE, A APLICAÇÃO CRIA UMA MENSAGEM ZNET_EXPLICIT PARA A SOLICITAÇÃO DOS NÓS VIZINHOS DOS ROTEADORES DA REDE
		
		int[] payload = new int[] { 0x76, 0x00 };			
		XBeeAddress64 addr64 = new XBeeAddress64(endereco[0], endereco[1], endereco[2], endereco[3], endereco[4], endereco[5], endereco[6], endereco[7]);			
			XBeeAddress16 addr16 = new XBeeAddress16(msb16, lsb16);						
			int sourceEndpoint = 0;						
			int destinationEndpoint = 0;			
			DoubleByte clusterId = new DoubleByte(0x31);			
			// Envio um frame para descobrir os vizinhos de um determinado nó solicitante
			ZNetExplicitTxRequest request = new ZNetExplicitTxRequest(0x01, addr64, addr16, 
						ZNetTxRequest.DEFAULT_BROADCAST_RADIUS, ZNetTxRequest.Option.UNICAST, payload, sourceEndpoint, destinationEndpoint, clusterId, ZNetExplicitTxRequest.zdoProfileId);
							
			xbee.sendSynchronous(request);	//ENVIO O FRAME		
			System.out.println("WAITING...");
			
			XBeeResponse packetResponse = null;		//ESPERO A RESPOSTA DA SOLICITAÇÃO
			while (true){
				XBeeResponse response = xbee.getResponse();
				if(response.getApiId() == ApiId.ZNET_EXPLICIT_RX_RESPONSE){
					System.out.println("---: "+response.toString());
					packetResponse = response;
					
					//primeiro verifico se o pacote recebido não foi uma solicitação de rotas de outro no roteador
					pacote = packetResponse.toString();					
					split = pacote.split("=");
					dados = split[8];
					d = dados.split(",");			
					packet = split[8];			
					ack = packet.split("s");			
					tabela = ack[0];			
					teste = tabela.split(",");
					
					if(teste[0].equals("0x76")){ //verifico se o frae recebido foi uma resposta da solicitação da tabela de vizinhos feita anteriormente
						break;
					}else{
						continue;
					}					
				}
				//Thread.sleep(200);
			}				
			
			int cont = 0;		
						
			//APÓS A CHEGADA DA RESPOSTA DE SOLICITAÇÃO DE VIZINHOS A APLICAÇÃO RECOLHE AS INFORMAÇÕES DO FRAME DE RESPOSTA
			for(int i = 0; i < teste.length; i++){
				
				if(teste[i].equals("0x13")){
					cont = i;				
					
					neighbors = "information";
					neighbors += ",";
					neighbors += teste[cont+3];
					neighbors += ",";
					neighbors += teste[cont+2];
					neighbors += "/";
					neighbors += teste[cont+1];
					neighbors += ",";
					neighbors += teste[cont];
					neighbors += ",";
					neighbors += teste[cont-1];
					neighbors += ",";
					neighbors += teste[cont-2];
					neighbors += ",";						//Ex:  [null,0xd5,0xfe/0x13,0xa2,0x00,0x40,0xa5,0x9e,0xd8/0xff;null,0xfa,0xea/0x13,0xa2,0x00,0x40,0xa5,0x9e,0xd5/0xb9]
					neighbors += teste[cont-3];				//	   |------16b-----|---------------64b---------------|-LQI-|	
					neighbors += ",";
					neighbors += teste[cont-4];
					neighbors += ",";
					neighbors += teste[cont-5];
					neighbors += ",";
					neighbors += teste[cont-6];
					neighbors += "/";
					neighbors += teste[cont+7];
					neighbors += ";";
					
					cont = 0;
					
					n[cs] = neighbors;
					cs++;
					
				}
			}
			
			String junta = null;
			
			for(int i=0; i < n.length; i++){
				if(n[i] != "null"){
					junta += n[i];
				}
			}
			
			//nullinformation,0x00,0x00/0x00,0x13,0xa2,0x00,0x40,0xa5,0x9d,0xd6/0x7e;information,0x17,0x08/0x00,0x13,0xa2,0x00,0x40,0xa8,0x36,0xbc/0xfd;null
							
			cs = 0;
					
			String[] processa = junta.split(";");			
						
			for(int i = 0; i < processa.length; i++){				
								
				String pega = processa[i];										
				
				String[] separa = pega.split("/");
				
				if(separa.length != 3){
					break;
				}
				
				String en16 = separa[0];				
				String en64 = separa[1];
				String lqi = separa[2];
				
				//VERIFICO AS INFORMAÇÕES RECOLHIDAS DO ROTEADOR 64 BITS E 16 BITS E OS ENDEREÇOS DE SEUS VIZINHOS
				System.out.println("SomaSolicitate16: "+somaSolicitante16);
				System.out.println("SomaSolicitate64: "+somaSolicitante64);
				System.out.println("VIZINHO16: "+en16);
				System.out.println("VIZINHO64: "+en64);
				System.out.println("VIZINHOlqi: "+lqi);
				
				
				
				String[] addre16 = en16.split(",");
				String[] addre64 = en64.split(",");							
				
				somaVizinho16[i] = calcula.ReturnMSB16andLSB16(addre16[1]);	
				somaVizinho16[i] += calcula.ReturnMSB16andLSB16(addre16[2]);			
				
				if(somaVizinho16[i] == 0){
					somaVizinho16[i] = 111;
				}
				
				msbVizinho16[i] = calcula.ReturnMSB16andLSB16(addre16[1]);
				lsbVizinho16[i] = calcula.ReturnMSB16andLSB16(addre16[2]);				
				
				
				
				somaVizinho64[i] = calcula.ReturnMSB16andLSB16(addre64[5]);
				somaVizinho64[i] += calcula.ReturnMSB16andLSB16(addre64[7]);				
				
				
				this.lqi[i] = calcula.ReturnMSB16andLSB16(lqi);				
				
			}		
			
			
			
			
			if(Neighbors.get(somaSolicitante16) == null){ //Significa a primeira vez que aquele nó fez uma solicitação para a aplicação				
				
				Neighbors.put(somaSolicitante16, junta); //ESCREVE AS INFORMAÇÕES DE VIZINHOS DOS DETERMINADOS ROTEADORES NA ESTRUTURA DE DADOS
				//ESCREVE AS INFORMAÇÕES EM ARQUIVO DE LOG		
				bw.write("INFORMAÇÕES DO NODO: "+somaSolicitante16);
				bw.write("\r\n");
				bw.write("\r\n");
				bw.write("TABELA DE VIZINHOS:\r\n "+Neighbors.get(somaSolicitante16));
				bw.write("\r\n");
				bw.write("\r\n");
				//bw.close();
				
				//System.out.println("Vizinhos Router: "+Neighbors.get(somaSolicitante16));				
				
				//AGORA É O PROCESSO EM QUE A APLICAÇÃO CRIARÁ O DETERMINADO NÓ NA TOPOLOGIA COM SEUS RESPECTIVOS VIZINHOS			
				List<Node> tes = topo.getNodeList();
				Node vizinho;
				
				int existe = 0;
				
				for (int i = 0; i < tes.size(); i++){					
					if(tes.get(i).getAddress16() == somaSolicitante16){						
						//Quer dizer que o no roteador ja foi adicionado a topologia
						existe = 1;
						for(int j = 0; j < tes.size(); j++){
							if(tes.get(j).getAddress16() == somaVizinho16[j]){								
								vizinho = tes.get(j);
								tes.get(i).addEdge(vizinho, lqi[j]);								
							}							
						}
					}						
				}
				
				if(existe == 0){
					//Quer dizer que o nó roteador ainda náo foi adicionado a topologia
					topo.addNode(new Node(somaSolicitante16, somaSolicitante64));
					for(int i=0; i < somaVizinho16.length ; i++){
						topo.addEdge(somaSolicitante16, somaVizinho16[i], lqi[i]);						
						topo.setMsbELsb(somaVizinho16[i], msbVizinho16[i], lsbVizinho16[i]);
					}
				}							
				
			}else{ 	//Caso ja tenha um vizinho adicionada para aquele no solicitante entï¿½o eu faï¿½o a comparaï¿½ï¿½o				
				
				String process = ""+ Neighbors.get(somaSolicitante16);
				
				String[] separa = process.split("/");
				String[] separa2 = junta.split("/");
				
				String j1 = separa[0];
				String j2 = separa2[0];
				
				String[] sep = j1.split(",");
				String[] sep2 = j2.split(",");
				
				
								
				if (sep[1].equals(sep2[1]) && sep[2].equals(sep2[2])){ 
									
					System.out.println("NO_MODIFICATION");
					
				}else{				
					
					Neighbors.put(somaSolicitante16, junta);					
					
					topo.removeNode(somaSolicitante16);  //Removo o nó para atualiza-lo com os novos valores
					
					topo.addNode(new Node(somaSolicitante16, somaSolicitante64));					
					
					for (int i = 0; i < somaVizinho16.length;i++){
						topo.addEdge(somaSolicitante16, somaVizinho16[i], lqi[i]);
					}						
					
										
					
				}			
				
			}
			
		}		
	
	
	private void SendData(String data) throws XBeeException, InterruptedException, IOException{
		
		
		
		if(data.equals("NO_MODIFICATION")){ //Significa que nao houve modificação na tabela de rotas
			
			int[] payload = new int[] { 'n', 'o', 'm' };			
			XBeeAddress64 addr64 = new XBeeAddress64(endereco[0], endereco[1], endereco[2], endereco[3], endereco[4], endereco[5], endereco[6], endereco[7]);					
			XBeeAddress16 dest16 = new XBeeAddress16(msb16, lsb16);
			
			ZNetTxRequest request = new ZNetTxRequest(0, addr64, dest16, 0, ZNetTxRequest.Option.UNICAST, payload);			
			xbee.sendAsynchronous(request);
			Thread.sleep(3000);		
					
		}else if(data.equals("ROUTING")){
			//
			System.out.println("Se preparando para Enviar...");
			
			String separa = "" + TableRoute.get(somaSolicitante16);			
			String[] trata = separa.split("/");						
			// Na tabela de rotas da Estrutura de dados ficou dessa forma... [KEY, NUM_ADDRESES/Rota1,Rota2,...] EX: ROUTING...: 0/null,38&null,187/111
			//																	   |--------------String---------------|
			
			int[] frame = new int[10];
			
			int numAddresses = Integer.parseInt(trata[0]);
			
			//PRIMEIRAMENTE ENVIA PARA O ROTEADOR O NUMERO DE ENDEREÇOS PARA O ROTEAMENTO			
			
			frame[0] = 255;
			
			frame[1] = numAddresses;			
			
			XBeeAddress64 addr64 = new XBeeAddress64(endereco[0], endereco[1], endereco[2], endereco[3], endereco[4], endereco[5], endereco[6], endereco[7]);			
			XBeeAddress16 dest16 = new XBeeAddress16(msb16, lsb16);
			
			String ROTAS = trata[1];
			
			String[] trataROTAS = ROTAS.split(",");
						
			
			String ROTA1 = trataROTAS[0];
			//String ROTA2 = trataROTAS[2];
			
			
			int msb = topo.getMSB(Integer.parseInt(ROTA1));
			int lsb = topo.getLSB(Integer.parseInt(ROTA1));
			//int msb2 = topo.getMSB(Integer.parseInt(ROTA2));
			//int lsb2 = topo.getLSB(Integer.parseInt(ROTA2));
			
			frame[2] = msb;
			frame[3] = lsb;
			
			String valor = ""+somaRota;

			String valor16 = ""+ Addresses.get(valor); //Addresses.put(RouteAddress64,RouteAddress16+"/"+msb16+","+lsb16);
			
			String[] separ = valor16.split("/");
			
			String des16 = separ[1];
			
			String[] Msb_Lsb = des16.split(",");			
							
			int valor1 = Integer.parseInt(Msb_Lsb[0]);	
			int valor2 = Integer.parseInt(Msb_Lsb[1]);		
			
			frame[4] = valor1;
			frame[5] = valor2;
						
			ZNetTxRequest request = new ZNetTxRequest(0, addr64, dest16, 0, ZNetTxRequest.Option.UNICAST, frame);			
			xbee.sendAsynchronous(request);
			
			System.out.println("DADOS DE ROTEAMENTO ENVIADOS COM SUCESSO!!!");
			
			List<Node> topologia = topo.getNodeList(); 
			List<Edge> vizinhos;
			
			//ESCREVE AS INFORMAÇÕES EM AQUIVO DE LOG

			bw.write("-TOPOLOGIA-");
			bw.write("\r\n");
			
			for(Node node : topologia){				
				bw.write("NODO: ");
				bw.write("\r\n");
				bw.write("Endereco 16 bits: ");
				bw.write(node.getAddress16());
				bw.write("\r\n");
				bw.write("Endereco 64 bits: ");
				bw.write(node.getAddress64());
				bw.write("\r\n");
				bw.write("Endereco MSB: ");
				bw.write(node.getMsb16());
				bw.write("\r\n");
				bw.write("Endereco LSB: ");
				bw.write(node.getLsb16());
				bw.write("\r\n");
				vizinhos = node.getNeighbors();
				bw.write("Vizinhos: ");
				bw.write(" ");
				bw.write("\r\n");
				for(Edge n : vizinhos){					
					bw.write("No vizinho: ");
					bw.write(n.getNode().getAddress16());
					bw.write("\r\n");
					bw.write("LQI: ");
					bw.write(n.getWeight());
					bw.write("\r\n");
				}
				
			}
			
			
			bw.close();
			
		}		
	}
	
	
	private void ProcessoCoordenator() throws Exception{	
		//MÉTODO QUE RECOLHERÁ AS INFORMAÇÕES DO NÓ ROTEADOR, ENDEREÇO DE 64, 16, E OS ENDEREÇOS DE SEUS VIZINHOS PARA CRIAR UM NÓ VIRTUAL NA TOPOLOGIA VIRTUAL REPRESENTANDO O COORDENADOR COM SEUS VIZINHOS
		int[] enderecoCoordenador = new int[10];
		
		enderecoCoordenador[0] = calcula.ReturnMSB16andLSB16("0x00");						
		enderecoCoordenador[1] = calcula.ReturnMSB16andLSB16("0x13");						
		enderecoCoordenador[2] = calcula.ReturnMSB16andLSB16("0xa2");						
		enderecoCoordenador[3] = calcula.ReturnMSB16andLSB16("0x00");						
		enderecoCoordenador[4] = calcula.ReturnMSB16andLSB16("0x40");						
		enderecoCoordenador[5] = calcula.ReturnMSB16andLSB16("0xa5");						
		enderecoCoordenador[6] = calcula.ReturnMSB16andLSB16("0x9d");						
		enderecoCoordenador[7] = calcula.ReturnMSB16andLSB16("0xd6");
		
		this.msb16 = calcula.ReturnMSB16andLSB16("0x00");
		this.lsb16 = calcula.ReturnMSB16andLSB16("0x00");
		
		int[] payload = new int[] { 0x76, 0x00 };			
		XBeeAddress64 addr64 = new XBeeAddress64(enderecoCoordenador[0], enderecoCoordenador[1], enderecoCoordenador[2], enderecoCoordenador[3], enderecoCoordenador[4], enderecoCoordenador[5], enderecoCoordenador[6], enderecoCoordenador[7]);			
		XBeeAddress16 addr16 = new XBeeAddress16(msb16, lsb16);						
		int sourceEndpoint = 0;						
		int destinationEndpoint = 0;			
		DoubleByte clusterId = new DoubleByte(0x31);			
		// Envio um frame para descobrir os vizinhos de um determinado nï¿½ solicitante
		ZNetExplicitTxRequest request = new ZNetExplicitTxRequest(0x01, addr64, addr16, 
					ZNetTxRequest.DEFAULT_BROADCAST_RADIUS, ZNetTxRequest.Option.UNICAST, payload, sourceEndpoint, destinationEndpoint, clusterId, ZNetExplicitTxRequest.zdoProfileId);
						
		xbee.sendSynchronous(request);			
		System.out.println("WAITING...");
		
		XBeeResponse packetResponse = null;
		
		
		while (true){
			XBeeResponse response = xbee.getResponse();
			if(response.getApiId() == ApiId.ZNET_EXPLICIT_RX_RESPONSE){
				System.out.println("---: "+response.toString());
				packetResponse = response;
				
				//primeiro verifico se o pacote recebido não foi uma solicitação de rotas de outro no roteador
				pacote = packetResponse.toString();					
				split = pacote.split("=");
				dados = split[8];
				d = dados.split(",");			
				packet = split[8];			
				ack = packet.split("s");			
				tabela = ack[0];			
				teste = tabela.split(",");
				
				if(teste[0].equals("0x76")){ //verifico se o frae recebido foi uma resposta da solicitação da tabela de vizinhos feita anteriormente
					ProcessaVizinhosCoordenador(response);
					break;
				}else{
					continue;
				}					
			}
			//Thread.sleep(200);
		}
		System.out.println("Pacote com vizinhos recebido... Aguarde um instante...");		
			
		/*
		while (true){			
			switch (packetResponse.getApiId()){
			case ZNET_EXPLICIT_RX_RESPONSE:
				System.out.println("COORDENADOR ENTRA..."+ packetResponse);				
				ProcessaVizinhosCoordenador(packetResponse);
				break;
			default:								
				continue;
			}
		break;
		}		
		*/
		
		
	}
	
	private void ProcessaVizinhosCoordenador(XBeeResponse frame) throws XBeeException, InterruptedException, IOException{
		//MÉTODO PARA RECOLHER AS INFORMAÇÕES DOS VIZINHOS DO COORDENADOR E CRIAR O NÓ VIRTUAL
		pacote = frame.toString();					
		split = pacote.split("=");
		dados = split[8];
		d = dados.split(",");		
		packet = split[8];			
		ack = packet.split("s");			
					
		tabela = ack[0];		
		teste = tabela.split(",");
		
				
		String e64 = split[5];
		String[] end64 = e64.split(",");			
		this.address64 = end64[0] + "," + end64[1] + "," + end64[2] + "," + end64[3] + "," + end64[4] + "," + end64[5] + "," + end64[6] + "," + end64[7];	
		
		//------Bloco de cï¿½digo para se enviar AS MENSAGENS DE RESPOSTA e SOLICITAï¿½ï¿½O para o Solicitante
		endereco[0] = calcula.ReturnMSB16andLSB16(end64[0]);						//
		endereco[1] = calcula.ReturnMSB16andLSB16(end64[1]);						//
		endereco[2] = calcula.ReturnMSB16andLSB16(end64[2]);						//
		endereco[3] = calcula.ReturnMSB16andLSB16(end64[3]);						//
		endereco[4] = calcula.ReturnMSB16andLSB16(end64[4]);						//
		endereco[5] = calcula.ReturnMSB16andLSB16(end64[5]);						//
		endereco[6] = calcula.ReturnMSB16andLSB16(end64[6]);						//
		endereco[7] = calcula.ReturnMSB16andLSB16(end64[7]);						//
		//----------------------------------------------------------------------------
		
		//System.out.println("=-=-=> "+endereco[5]);
		//System.out.println("=-=-=> "+endereco[7]);
		
		somaSolicitante64 = endereco[5] + endereco[7]; 
									
		System.out.println("ENDERECO 64: "+ address64);		
					
		String e16 = split[6];
		String[] end16 = e16.split(",");						
		this.msb16 = calcula.ReturnMSB16andLSB16(end16[0]);
		this.lsb16 = calcula.ReturnMSB16andLSB16(end16[1]);			
		somaSolicitante16 = calcula.ReturnMSB16andLSB16(end16[0]) + calcula.ReturnMSB16andLSB16(end16[1]);		
		
		if(somaSolicitante16 == 0){
			somaSolicitante16 = 111;
		}
		
		String test = ""+ somaSolicitante16;	
					
			
			int cont = 0;			
			
			for(int i = 0; i < teste.length;i++){				
				if(teste[i].equals("0x13")){					
					cont = i;
					
					neighbors = "information";
					neighbors += ",";
					neighbors += teste[cont+3];
					neighbors += ",";
					neighbors += teste[cont+2];
					neighbors += "/";
					neighbors += teste[cont+1];
					neighbors += ",";
					neighbors += teste[cont];
					neighbors += ",";
					neighbors += teste[cont-1];
					neighbors += ",";
					neighbors += teste[cont-2];
					neighbors += ",";						//Ex:  [null,0xd5,0xfe/0x13,0xa2,0x00,0x40,0xa5,0x9e,0xd8/0xff;null,0xfa,0xea/0x13,0xa2,0x00,0x40,0xa5,0x9e,0xd5/0xb9]
					neighbors += teste[cont-3];				//	   |------16b-----|---------------64b---------------|-LQI-|	
					neighbors += ",";
					neighbors += teste[cont-4];
					neighbors += ",";
					neighbors += teste[cont-5];
					neighbors += ",";
					neighbors += teste[cont-6];
					neighbors += "/";
					neighbors += teste[cont+7];
					neighbors += ";";
					
					cont = 0;
					
					n[cs] = neighbors;
					cs++;
					
				}				
			}
			
			String junta = null;
			
			for(int i=0; i<n.length;i++){
				if(n[i] != "null"){
					junta += n[i];
				}
			}
			
			cs = 0;		
			
			String[] processa = junta.split(";");		
			
			for(int i = 0; i < processa.length; i++){	
								
				String pega = processa[i];				
				
				String[] separa = pega.split("/");
				
				if(separa.length != 3){
					break;
				}			
								
				String en16 = separa[0];				
				String en64 = separa[1];
				String lqi = separa[2];
				
				String[] addr16 = en16.split(",");
				String[] addr64 = en64.split(",");							
				
				somaVizinho16[i] = calcula.ReturnMSB16andLSB16(addr16[1]);	
				somaVizinho16[i] += calcula.ReturnMSB16andLSB16(addr16[2]);			
				
				msbVizinho16[i] = calcula.ReturnMSB16andLSB16(addr16[1]);
				lsbVizinho16[i] = calcula.ReturnMSB16andLSB16(addr16[2]);
				
				somaVizinho64[i] = calcula.ReturnMSB16andLSB16(addr64[5]);
				somaVizinho64[i] += calcula.ReturnMSB16andLSB16(addr64[7]);
				
				this.lqi[i] = calcula.ReturnMSB16andLSB16(lqi);			
				
				}		
			
			if(Neighbors.get(somaSolicitante16) == null){ //significa que nï¿½o hï¿½ ainda uma tabela de vizinhos formada para aquele no solicitante				
				
								
				Neighbors.put(somaSolicitante16, junta); // Dï¿½VIDAS... AQUI ESTOU CRIANDO A TABELA DE ROTAS PARA O DETERMINADO Nï¿½ SOLICITANTE	
				
				System.out.println("ESTRUTURA DE VIZINHOS: "+Neighbors.get(somaSolicitante16));
				
				bw.write("INFORMAÇÕES DO NODO: "+somaSolicitante16);
				bw.write("\r\n");
				bw.write("\r\n");
				bw.write("TABELA DE VIZINHOS:\r\n "+Neighbors.get(somaSolicitante16));
				bw.write("\r\n");
				bw.write("\r\n");
				//bw.close();
				
				topo.addNode(new Node(somaSolicitante16, somaSolicitante64));				
				
				topo.setMsbELsb(somaSolicitante16, msb16, lsb16);
				
				
				for (int i = 0; i < somaVizinho16.length; i++){					
					if(somaVizinho16[i] != 0){					
						topo.addNode(new Node (somaVizinho16[i], somaVizinho64[i]));
						topo.setMsbELsb(somaVizinho16[i], msbVizinho16[i], lsbVizinho16[i]);
					}					
				}			
				
				nodes = topo.getNodeList();		
				
				for(int i=0; i < somaVizinho16.length; i++){
					topo.addEdge(somaSolicitante16, somaVizinho16[i], lqi[i]);					
				}				
				
			}else{ 	//Caso ja tenha um vizinho adicionada para aquele no solicitante entï¿½o eu faï¿½o a comparaï¿½ï¿½o				
				
				String process = ""+ Neighbors.get(somaSolicitante16);			
								
				if (neighbors != process){ //Isso significa que a tabela de vizinhos nï¿½o ï¿½ a mesma entï¿½o deve-se fazer um novo calculo de rotas				
					
					Neighbors.put(somaSolicitante16, neighbors);					
					
					topo.removeNode(somaSolicitante16);  //Removo o nó para atualiza-lo com os novos valores
					
					topo.addNode(new Node(somaSolicitante16, somaSolicitante64));					
					
					for (int i = 0; i < somaVizinho16.length;i++){
						topo.addEdge(somaSolicitante16, somaVizinho16[i], lqi[i]);
					}					
					
				}else{					
					System.out.println("NO_MODIFICATION");
			}				
		}
	}
	
	
private void ProcessaRouteRecord(int[] packetInformation) throws Exception{
//ESSE MÉTODO IRÁ CAPTURAR AS INFORMAÇÕES CONTIDAS NO PACOTE ROUTE RECORD INDICATOR		
		String rotas = null;
		int cont = 0;
		int numAddreses = 0;
		
		
		for(int i = 0; i < packetInformation.length; i++){
			System.out.println("FRAME: "+ i +" "+ packetInformation[i]);
		}
		
		somaSolicitante64 = 0;
		
		for(int i = 0; i < packetInformation.length; i++){
			
			if(i == 3 || i == 4 || i == 5 || i == 6 || i == 7 || i == 8 || i == 9 || i == 10){
				endereco[cont] = packetInformation[i];				
				somaSolicitante64 += endereco[cont];							
				cont++;
			}
		
			if(i == 8){
				RouteAdd64 = packetInformation[i];
			}
			if(i == 10){
				RouteAdd64 += packetInformation[i];
			}			
			if(i == 11){
				RouteAddress16 = ""+packetInformation[i];
				msb16 = packetInformation[i];
			}
			if(i == 12){
				RouteAddress16 += ","+packetInformation[i];
				lsb16 = packetInformation[i];
			}			
			if(i == 14){
				numAddreses = packetInformation[i];
			}
			
			if(i > 14 && i < packetInformation.length - 1){
				rotas += ","+ packetInformation[i];
			}			
			
		}
		//O CÓDIGO ACIMA RECUPERA AS INFORMAÇÕES DE ROTEADOR FONTE DAS INFORMAÇÕES, ENDEREÇO DE 64 BITS E 16 BITS DELE, NUMERO DE ENDEREÇOS POR ONDE O FRAME TRAFEGOU, E OS ENDEREÇOS DE 16 BITS, DOS ROTEADORES POR ONDE O FRAME TRAFEGOU
		
		//SOMO O ENDEREÇO DE 16 BITS		
		somaSolicitante16 = msb16 + lsb16;
		
		RouteAddress64 = ""+RouteAdd64;
		
		for(int i = 0; i < endereco.length; i++){
			endereco64 += ","+endereco[i];
		}
		
		endereco16 += ""+ msb16;
		endereco16 += ","+ lsb16;
		

		String verifica = ""+ TableRoute.get(somaSolicitante16);
		String verifica2 = ""+Addresses.get(RouteAddress64);
		
		//VERIFICO SE JÁ TENHO AS INFORMAÇÕES DAQUELE ROTEADOR ADICIONADO A ESTRUTURA DE DADOS, SE JÁ TEM ENTÃO É PRA SUPOR QUE JÁ TENHA ADICIONADO ELE À TOPOLOGIA VIRTUAL
		if(!verifica.equals("null") || !verifica2.equals("null")){
			//ESCREVO AS INFORMAÇÕES NO ARQUIVO DE LOGS			
			bw.write("TABELA DE ROTAS!");
			bw.write("\r\n");
			bw.write("\r\n");
			
			String mostra = ""+ TableRoute.get(somaSolicitante16); 
			
			bw.write(RouteAdd64);
			bw.write("\r\n");
			bw.write(mostra);
			bw.write("\r\n");
			bw.write("TABELA DE ENDEREÇOS!");
			bw.write("\r\n");
			bw.write("\r\n");
			
			String mostra2 = ""+ Addresses.get(RouteAddress64);
			bw.write(mostra2);
			bw.write("\r\n");
			bw.write("\r\n");
			
			System.out.println("TABELA DE ROTAS: "+TableRoute.get(somaSolicitante16));
			System.out.println("TABELA DE ENDERECOS: "+Addresses.get(RouteAddress64));		
			
			//SE O NUMERO DE ENDEREÇOS SEJA IGUAL A ZERO ENTÃO O FRAME FOI ENVIADO DIRETO PARA O COORDENADOR	
			if(numAddreses == 0){
				ProcessVizinhosRouters();		
			}else{
				CreateSourceRoute(); //CASO SEJA DIFERENTE DE 0 ENTÃO HOUVE SALTOS ATE CHEGAR AO COODENADOR, E AGORA A APLICAÇÃO NECESSITA CRIAR UMA COMUNICAÇÃO PARA AQUELE ROUEADOR SOLICITANTE
			}
			
		}else{
			//	
			String solic = ""+ somaSolicitante16;
			
			//ADICIONO AS INFORMAÇÕES ÀS ESTRUTURAS DE DADOS
			TableRoute.put(solic, numAddreses+"/"+rotas); // E.g: 1/null,214,72
			Addresses.put(RouteAddress64, somaSolicitante16 + "/" + msb16 + "," + lsb16);
			
			if(numAddreses == 0){
				ProcessVizinhosRouters();		
			}else{
				CreateSourceRoute();
			}
		}			
			
	}
	
	
	private void CreateSourceRoute() throws Exception{	
		//NESSE MÉTODO EU CAPTURO AS INFORMAÇÕES QUE NECESSITO PARA CRIAR UMA COMUNICAÇÃO COM O ROTEADOR, COMO A API XBEE NÃO VEM IMPLEMENTADO UM MÉTODO NATIVO PARA SE TRAVALHAR COM AS MENSAGENS CREATE SOURCE ROUTE ENTÃO NÓS RESOLVEMOS ESCREVER AS INFOAMAÇÕES DO FRAME CRIADO DIRETAMENTE NA SERIAL PARA O MÓDULO XBEE INTERPRETAR
		String teste = ""+TableRoute.get(somaSolicitante16);
		String[] separa = teste.split("/");
		
		int numAddr = Integer.parseInt(separa[0]); //numero de saltos ate o destino
		
		String addreses = separa[1];
		
		String[] separa2 = addreses.split(",");
		
		int msb = 0;
		int lsb = 0;
		int msb2 = 0;
		int lsb2 = 0;			
		
		//VERIFICA QUANTOS ENDEREÇOS DE SALTOS SERÁ
		if(separa2.length == 2){
			msb = Integer.parseInt(separa2[1]);
			lsb = Integer.parseInt(separa2[2]);
		}else if (separa2.length == 4){
			msb = Integer.parseInt(separa2[1]);
			lsb = Integer.parseInt(separa2[2]);
			msb2 = Integer.parseInt(separa2[3]);
			lsb2 = Integer.parseInt(separa2[4]);
		}	
		

		//CRIA O FRAME
		int a = 0x7e;
		int b = 0x00;
		int c = 0x10;
		int d = 0x21;
		int e = 0x00;
		//Address 64 bits
		int f = endereco[0];
		int g = endereco[1];
		int h = endereco[2];
		int i = endereco[3];
		int j = endereco[4];
		int k = endereco[5];
		int l = endereco[6];
		int m = endereco[7];
		//Address 16 bits
		int n = msb16;
		int o = lsb16;
		//Route Command Option - Default 0
		int p = 0x00;		
		//Number of Addresses
		//int q = 0x00;
		//Addresses		
		
					
		
		if(separa2.length == 2){
			
			//CASO O NUMERO DE SALTOS ATE O ROTEADOR SEJA 1 ENTÃO ELE ENTRA NESSA CONDIÇÃO
			int[] preFrame = new int[]{d,e,f,g,h,i,j,k,l,m,n,o,p,numAddr,msb,lsb};
			int soma = 0;
			
			for(int z = 0; z < preFrame.length; z++){
				if(preFrame[z] != 0){
					soma += preFrame[z];
				}else{
					continue;
				}
			}
			
			int checksum = (0xff - (soma & 0xff)); //CALCULA Checksum DO FRAME
			
			int[] frame2 = new int[]{a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,numAddr,msb,lsb,checksum}; //CRIA UM ARRAY COM OS VALORES DO FRAME CREATE SOURCE ROUTE
			xbee.sendPacket(frame2); //ESCREVE O VALOR NA SERIAL ATRAVÉS DO SENDPACKET


			//ESCREVE AS INFOAMAÇÕES EM ARQUIVO DE LOG
			bw.write("FRAME CREATE SOURCE ROUTE!");
			bw.write("\r\n");
			bw.write("\r\n");
			for(int x = 0; i < frame2.length; x++){
				bw.write(frame2[x]);
				bw.write("\r\n");
			}
			
			
		}else if(separa2.length == 4){
			
			//CASO SEJA 2 SALTOS ATÉ O DESTINO
			int[] preFrame = new int[]{d,e,f,g,h,i,j,k,l,m,n,o,p,numAddr,msb,lsb,msb2,lsb2};
			int soma = 0;
			
			for(int z = 0; z < preFrame.length; z++){
				if(preFrame[z] != 0){
					soma += preFrame[z];
				}else{
					continue;
				}
			}
			
			int checksum = (0xff - (soma & 0xff)); //CÁLCULO Checksum DO FRAME
			
			int[] frame = new int[]{a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,numAddr,msb,lsb,msb2,lsb2,checksum}; //FRAME		
			xbee.sendPacket(frame); //ENVIO

			//ESCREVE AS INFORMAÇÕES EM ARQUIVO DE LOG

			bw.write("FRAME CREATE SOURCE ROUTE!");
			bw.write("\r\n");
			bw.write("\r\n");
			for(int x = 0; i < frame.length; x++){
				bw.write(frame[x]);
				bw.write("\r\n");
			}
		}
		
		//VERIFICAR AQUI SE O ENVIO DO PACOTE CREATE SOURCE ROUTE CRIA UMA MENSAGEM DE AVISO QUE O PACOTE FOI RECEBEIDO COM SUCESSO	
		
		System.out.println("CREATE SOURCE ROUTE ENVIADO COM SUCESSO!!");
		
		Thread.sleep(2000);
				
		ProcessVizinhosRouters(); //CHAMA MÉTODO
		
	}
	
private void ProcessaMensagem(String[] rota) throws Exception{
	
	//O VALOR CONTIDO NO PAYLOAD DE DADOS DO FRAME DE SOLICITAÇÃO DE ROTAS É UM VALOR PRE DEFINIDO, É APENAS OS VALORES DE 5º E 7º VALORES DO ENDEREÇO DE 64 BITS PARA O DESTINO				
	for(int i = 2;i < rota.length;i++){			
		somaRota += calcula.ReturnMSB16andLSB16(rota[i]);
		System.out.println(rota[i]);
	}
	String valor = ""+somaRota;		
	//RECOLHO O VALOR DO ENDEREÇO DE DESTINO 16 BITS DE DESTINO, COM O VALOR ENVIADO DO ROTEADOR SOLICITANTE QUE É O DESTINO 64 BITS
	String valor16 = ""+ Addresses.get(valor); //Addresses.put(RouteAddress64,RouteAddress16+"/"+msb16+","+lsb16);	
	
	System.out.println("VERIFICA..."+valor16);
	
	//SEPARO OS DOIS VALORES DO ENDEREÇO DE 16 BITS		
	String[] separa = valor16.split("/");		
		
	
	
	String dest16 = separa[1];
	
	String[] Msb_Lsb = dest16.split(",");
	
	//CONVERTO OS DOIS VALORES DE 16 BITS DESTINO PARA INTEIRO				
	int valor1 = Integer.parseInt(Msb_Lsb[0]);	
	int valor2 = Integer.parseInt(Msb_Lsb[1]);
	

	//SOMO OS DOIS VALORES DE DESTINO 16 E PASSO ESSA SOMA PARA O MÉTODO ROUTE CALCULATE
	int somaValores = valor1 + valor2;
	
	RouteCalculate(somaValores);		
		
	} 
	
private SDNCoordinator() throws Exception {
				
	int contRoteador = 0;		
	int teste;
	int[] frameRouteRecord = null;
	int contador = 0;
	XBeeResponse unknown = null;
	XBeeResponse explicit = null;
		
	try{
		xbee.open("COM4", 9600);		//INICIA UMA COMUNICAÇÃO COM O XBEE
		ProcessoCoordenator();			//CHAMA O MÉTODO PARA ADICIONAR O COORDENADOR À TOPOLOGIA
			
							//CONDIÇÃO PARA CRIAÇÃO DO ARQUIVO QUE GUARDA OS LOGS DA EXECUÇÃO
		if (!file.exists()) {
			file.createNewFile();
		}			
			
								
		while (true){ 
		//DENTRO DO LAÇO DE REPETIÇÃO ELE ESPERA CHEGAR PACOTES					
			XBeeResponse response = xbee.getResponse();	
		
		//NA CHEGADA DE UM FRAME ELE VERIFICA O TIPO DO PACOTE, NO NOSSO CASO, COMO AINDA NÃO FOI DEFINIDO O TIPO ROUTE RECORD INDICATOR PELA API, ENTÃO ELE VEM COM O TIPO DESCONHECIDO, "UNKNOWN".
			if(response.getApiId() == ApiId.UNKNOWN){
				unknown = response;
				contador++;
				continue;
			}
			
					
			else if(response.getApiId() == ApiId.ZNET_EXPLICIT_RX_RESPONSE){
				explicit = response;
				contador++;			
				
				//ESCREVO NO ARQUIVO DE LOGS AS INFORMAÇÕES ABAIXO
				bw.write("FRAME ROUTE RECORD INDICATOR");
				bw.write("\r\n");
				bw.write("\r\n");											
				
				//COMO O PRIMEIRO FRAME A CHEGAR É O ROUTE RECORD ENTÃO NA CHEGADA DO FRAME ZNET_EXPLICIT EU JA TEREI AS INFORMAÇÕES
				frameRouteRecord = unknown.getProcessedPacketBytes();
				ProcessaRouteRecord(frameRouteRecord); //Primeiramente recupero as informações do Pacote Route Record
				
				//ESCREVO AS INFORMAÇÕES DO FRAME EM ARQUIVO DE LOG		
				for(int i = 0; i < frameRouteRecord.length; i++){
					bw.write(frameRouteRecord[i]);
					bw.write("\r\n");
				}
				

				//UTILIZAMOS BASTANTE SPLIT PARA CAPTURAR INFORMAÇÕES ESPECÍFICAS DOS FRAMES, NESTE CASO DO PACOTE ZNET_EXPLICIT		
				pacote = explicit.toString();					
				split = pacote.split("=");
				dados = split[8];
				d = dados.split(",");		
				packet = split[8];			
				ack = packet.split("s");				
				tabela = ack[0];				
				String[] pegaRota = tabela.split(",");
				

				//VERIFICAMOS SE A INFORMAÇÃO CONTIDA NO PAYLOAD DE DADOS É UMA SOLICITAÇÃO DE ROTAS DE ALGUM ROTEADOR							
				if(pegaRota[0].equals("0x4f") && pegaRota[1].equals("0x4b")){
					
					//CASO SEJA, ELE ESCREVE AS INFORMAÇÕES ABAIXO EM AQUIVO DE LOG 	
					bw.write("PACOTE DE SOLICITACAO DE ROTA!");
					bw.write("\r\n");
					bw.write("\r\n");
					bw.write(pacote);							
					bw.write("DAODS DO PACOTE!");
					bw.write("\r\n");
					bw.write("\r\n");
					bw.write(pegaRota[0]);
					bw.write("\r\n");
					bw.write(pegaRota[1]);
					bw.write("\r\n");
					bw.write(pegaRota[2]);
					bw.write("\r\n");
					bw.write(pegaRota[3]);
					bw.write("\r\n");							
 					String soma = ""+ calcula.ReturnMSB16andLSB16(pegaRota[2]) + calcula.ReturnMSB16andLSB16(pegaRota[3]);					
					//VERIFICA SE O VALOR É DIFERENTE DE NULL E CASO SEJA PASSA POR PARAMETRO PARA O METODO PROCESSA MENSAGEM, CASO CONTRÁRIO ELE CONTINUA ESPERANDO FRAMES	
					String verifica = ""+ Addresses.get(soma);
					if(!verifica.equals("null")){
						ProcessaMensagem(pegaRota);
					}else{
						continue;
					}							
					}											
				
					contador = 0;
				}				
							
			}
			
			
		}finally {			
			xbee.close(); //FECHA A CONEXÃO
		}
		
		
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure("log4j.properties");	
		new SDNCoordinator(); //starto a minha classe e chamo o construtor para executar
		
			
	}	
	
}
