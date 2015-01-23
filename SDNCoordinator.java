package com.TCC2.sistema;



import grafo.Node;

import grafo.Topology;



import java.util.Enumeration;

import java.util.Hashtable;

import java.util.List;



import org.apache.log4j.Logger;

import org.apache.log4j.PropertyConfigurator;



import routing.GenerateRoute;



import com.rapplogic.xbee.api.ApiId;

import com.rapplogic.xbee.api.RemoteAtRequest;

import com.rapplogic.xbee.api.RemoteAtResponse;

import com.rapplogic.xbee.api.XBee;

import com.rapplogic.xbee.api.XBeeAddress16;

import com.rapplogic.xbee.api.XBeeAddress64;

import com.rapplogic.xbee.api.XBeePacket;

import com.rapplogic.xbee.api.XBeeResponse;

import com.rapplogic.xbee.api.wpan.TxRequest16;

import com.rapplogic.xbee.api.wpan.TxRequest64;

import com.rapplogic.xbee.api.zigbee.ZNetExplicitRxResponse;

import com.rapplogic.xbee.api.zigbee.ZNetExplicitTxRequest;

import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;

import com.rapplogic.xbee.util.DoubleByte;



public class SDNCoordinator {

	

	private int route;

	private int routerAddressInteger64 = 0x40a59ed5;

	

	Hashtable balance = new Hashtable();

    Enumeration names;

    

    

    private String solicitante;    

	

	public String getSolicitante() {

		return solicitante;

	}



	public void setSolicitante(String solicitante) {

		this.solicitante = solicitante;

	}



	public int getRoute() {

		return route;

	}



	public void setRoute(int route) {

		this.route = route;

	}



	private final static Logger log = Logger.getLogger(Sistema.class);

	

	private void RouteCalculate(int address16) throws InterruptedException{

		

		System.out.println("STARTING ROUTE CALCULATION PROCESS...");

		Thread.sleep(3000);

		

		int coordenatorAddressInteger64 = 0x40a59dd6;

		int coordenatorAddressInteger16 = 0x00;

		

	    Topology topo = new Topology();

	    GenerateRoute route = new GenerateRoute();	  

	    

	    Node router = new Node(address16, routerAddressInteger64); // Crio dois nós um roteador e coordenador

	    Node coordenator = new Node(coordenatorAddressInteger16, coordenatorAddressInteger64); 

	    

	    router.addEdge(coordenator, 2); //digo quem são os vizinhos de cada nó e os custos

	    coordenator.addEdge(router, 2);

	    

	    topo.addNode(router); //Crio uma topologia

	    topo.addNode(coordenator);

	    

	    //CALCULO DA MENOR ROTA ENTRE OS DOIS NÓS	

	    

	    List<Integer> sp = route.requestShortestPath(topo, address16, coordenatorAddressInteger16);

		

		System.out.println("Printing the lower route...");

		

		int rota = 0;

		

		for (int i = 0; i < sp.size(); i++) {

			System.out.println("No "+i+": "+sp.get(sp.size()-i-1));

			rota = sp.get(sp.size()-i-1);

		}

		

		if(rota != 0){

			setRoute(rota);

		}else{

			setRoute(0);

		}		

	}

	

	private void DataEstructure(String address64, String[] address16) throws InterruptedException{

		

	    Endereco add16 = new Endereco();					    

	    int[] addressIntegerRouter16 = add16.RetornaEndereco(address16[0], address16[1]); //Envia para a classe Endereco as informações de endereco de 16 bits em string e retorna o valor correspondente em inteiro

		

	    int somaAddressRouter16 = addressIntegerRouter16[0] + addressIntegerRouter16[1]; //Aqui é feito um somatório dos dois valores para o armazenamento na ED

	    

	    System.out.println("ENDEREÇO INTEIRO 16 ROTEADOR: "+ addressIntegerRouter16[0] + "," + addressIntegerRouter16[1]);

	    //System.out.println("SOMATORIO: "+ somaAddressRouter16);

	    //ARMAZENAMENTO DE INFORMAÇÕES NA ED

	    

	    balance.put(addressIntegerRouter16, new String(address64)); //armazeno as informações de 16 chaveadas em endereço de 64

	    balance.put(somaAddressRouter16, addressIntegerRouter16);  //também faço um chaveamento da soma dos valores inteiros do endereço de 16 bits com o array de endereço de 16

	    

	    RouteCalculate(somaAddressRouter16);

	    

	}

	

	private void CollectInformation(XBeeResponse frame) throws InterruptedException{

		

		String pacote = frame.toString();					

		String[] split = pacote.split("=");

		String e64 = split[5];

		String[] end64 = e64.split(",");

		

		String addressStringRouter64 = end64[0] + "," + end64[1] + "," + end64[2] + "," + end64[3] + "," + end64[4] + "," + end64[5] + "," + end64[6] + "," + end64[7];

		

		System.out.println("ENDEREÇO 64 ROTEADOR: "+ addressStringRouter64);

		

		setSolicitante(addressStringRouter64); // seto o endereço de 64 bits tara uma variável global do solicitante atual, esta tariável setá usada mais adiante

		

		String e16 = split[6];

		String[] end16 = e16.split(",");

		

		String addressStringRouter16 = end16[0] + "," + end16[1];

		

		System.out.println("ENDEREÇO 16 ROTEADOR: "+ addressStringRouter16);

		

		String dados = split[8];

		String[] d = dados.split(",");

		String data = d[0] + "," + d[1];

		

		System.out.println("DADOS RECEBIDOS: "+ data);	

		

		if (data.equals("0x4f,0x49")){

			DataEstructure(addressStringRouter64, end16);

		}else{		

			System.out.println("CHEGOU UMA MENSAGEM DE NÃO SOLICITAÇÃO DE ROTA!");

		}

	}

	

	private void ReceivePacket(XBeeResponse response) throws InterruptedException{

		

		switch (response.getApiId()){

		

	    case ZNET_TX_REQUEST:

	    	System.out.println("FOI RECEBIDO UM FRAME (ZNET_TX_REQUEST): "+ response.toString());

	      break;

	    case ZNET_EXPLICIT_TX_REQUEST:

	    	System.out.println("FOI RECEBIDO UM FRAME (ZNET_EXPLICIT_TX_REQUEST): "+ response.toString());

	      break;				    

	    case TX_STATUS_RESPONSE:

	    	System.out.println("FOI RECEBIDO UM FRAME (TX_STATUS_RESPONSE): "+ response.toString());

	      break;				    

	    case ZNET_RX_RESPONSE:

	    	System.out.println("FOI RECEBIDO UM FRAME (ZNET_RX_RESPONSE): "+ response.toString());

	      break;

	    case ZNET_EXPLICIT_RX_RESPONSE:

	    	System.out.println("FOI RECEBIDO UM FRAME (ZNET_EXPLICIT_RX_RESPONSE): "+ response.toString());

	    	CollectInformation(response);

	    	

	    case ZNET_TX_STATUS_RESPONSE:

	    	System.out.println("FOI RECEBIDO UM FRAME (ZNET_TX_STATUS_RESPONSE): "+ response.toString());

	      break;

	    case REMOTE_AT_RESPONSE:

	    	System.out.println("FOI RECEBIDO UM FRAME (REMOTE_AT_RESPONSE): "+ response.toString());

	      break;			   

	    case UNKNOWN:

	    	System.out.println("FOI RECEBIDO UM FRAME DESCONHECIDO (UNKNOWN): "+ response.toString());

	      break;

	    case ERROR_RESPONSE:

	    	System.out.println("OCORREU UM ERRO DE RESPOSTA (ZNET_EXPLICIT_TX_REQUEST): "+ response.toString());

	    	break;

	}	

	}

	

	private SDNCoordinator() throws Exception {

		

		XBee xbee = new XBee();

				

		XBeeAddress64 addrRouter64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0xa5, 0x9e, 0xd5);

			

		

		

		try{

			xbee.open("COM4", 9600);	

					

			while (true){

				XBeeResponse response = xbee.getResponse();

								

				System.out.println("PACOTE: " +response.toString());

				

				ReceivePacket(response);				

							

				System.out.println("WAITING...");

							

				Thread.sleep(3000);

				

											

				int[] payload = new int[] { route };							

							

				int sourceEndpoint = 0;

				int destinationEndpoint = ZNetExplicitTxRequest.Endpoint.DATA.getValue();

							

				DoubleByte clusterId = new DoubleByte(0x0, ZNetExplicitTxRequest.ClusterId.SERIAL_LOOPBACK.getValue());

				

				int[] address16 = (int[]) balance.get(solicitante);

				

				XBeeAddress16 addrRouter16 = new XBeeAddress16(address16);

							

				ZNetExplicitTxRequest request = new ZNetExplicitTxRequest(0xff, addrRouter64, addrRouter16, 

							ZNetTxRequest.DEFAULT_BROADCAST_RADIUS, ZNetTxRequest.Option.UNICAST, payload, sourceEndpoint, destinationEndpoint, clusterId, ZNetExplicitTxRequest.znetProfileId);

							

							

				xbee.sendAsynchronous(request);

							

				XBeeResponse r = xbee.getResponse();

							

				if (r.getApiId() == ApiId.ZNET_EXPLICIT_RX_RESPONSE) {

					ZNetExplicitRxResponse rx = (ZNetExplicitRxResponse) response;							

					log.info("--------------------------------------------RECEIVED EXPLICIT PACKET RESPONSE-------------------------------------/n " + response.toString());

					xbee.close();

				} else {

					log.debug("RECEIVED UNEXPECTED PACKET: " + response.toString());

				}

			}

		}finally {

			xbee.close();

		}

	}



	public static void main(String[] args) throws Exception {

		// TODO Auto-generated method stub

		PropertyConfigurator.configure("log4j.properties");  

		

		new SDNCoordinator();

	}



}
