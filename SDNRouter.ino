#include <XBee.h>
#include <HashMap.h>

XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
ZBExpRxResponse rx = ZBExpRxResponse();

const byte HASH_SIZE = 10; 


//APENAS INICIAMENTE CRIAMOS VÁRIAS ESTRUTURAS DE DADOS QUE ARMAZENARÁ AS INFORMAÇÕES DE NUMERO DE ENDEREÇOS ATE O DESTINO, E OS ENDEREÇOS DE SALTOS MSB E LSB
HashType<int,int> hashRawArray[HASH_SIZE]; 
HashMap<int,int> NumeroAddreses = HashMap<int,int>( hashRawArray , HASH_SIZE ); 
HashMap<int,int> msb = HashMap<int,int>( hashRawArray , HASH_SIZE );
HashMap<int,int> lsb = HashMap<int,int>( hashRawArray , HASH_SIZE );
HashMap<int,int> msb16 = HashMap<int,int>( hashRawArray , HASH_SIZE );
HashMap<int,int> lsb16 = HashMap<int,int>( hashRawArray , HASH_SIZE );


int MSB = 0;
int LSB = 0;

int MSB16 = 0;
int LSB16 = 0;

int rota[10];
int contadorFrames = 0;
int frame[10];
int numAddresses;
int tamanho;
int contador = 0;
int contadorEnvia = 0;
int somaAddress16 = 0; //definir aqui a soma do endereco de destino para onde quer se enviar a mensagem


void setup(){
  Serial.begin(9600);	//INICIO A COLUNICAÇÃO COM O MÓDULO XBEE
  xbee.begin(Serial);
  pinMode(10,OUTPUT);	//INDICO O PINO DIGITAL EM QUE A LED ESTÁ CONECTADA
}

void loop(){ 
  Recebe();    
  
  //ESPERA UM PACOTE DENTRO DE UM LAÇO DE REPETIÇÃO
}

void Recebe(){  
  xbee.readPacket();
  if (xbee.getResponse().isAvailable()) {      
      if (xbee.getResponse().getApiId() == ZB_EXPLICIT_RX_RESPONSE) {  //CASO O FRAME RECEBIDO SEJA UM ZB_EXPLICIT ELE FARÁ UMA VERIFICAÇÃO LOGO ABAIXO        
          xbee.getResponse().getZBExpRxResponse(rx);
                             
          for (int i = 0; i <= rx.getDataLength(); i++) {           
            frame[i] = rx.getData()[i], DEC;           
            if(i == rx.getDataLength()){              
                if(frame[0] == 255){ //Significa que é um frame com a decisão de rotas
                  VerificaFrame();
                }else{                //Caso contrário é um comando para ação na placa arduino
                  InterpretaComando();
                }               
            }          
          }          
          
     }else if(xbee.getResponse().getApiId() == 163){  //CASO NÃO SEJA UM FRAME ZB_EXPLICIT ENTÃO SE FOR DE ID 163 ENTÃO É UM FRAME ENVIADO DO COORDENADOR MANY-TO-ONE          
        
        Serial.println("MANY TO ONE REQUEST INDICATOR");
        Serial.println("CONTADOR: ");
        Serial.println(contador);
        Serial.println(" ");
        
        /***********ESSA PARTE DO CÓDIGO É DIFERENTE PARA CADA ROTEADOR DA REDE*********************************************/
	//AS CONDIÇÕES SÃO CONTADORES QUE ENVIAM MENSAGENS EM PERÍODOS PRE DETERMINADOS

        if(contador == 3){ 
          //Mensagem();          


//O metodo Mensagem é para os rotadores que apenas irão enviar as mensagens pre definidas para o coordenador para gerar o route record, CASO SEJA APENAS ESSE ROTEADOR DESMARQUE ESSE MÉTODO                    
          //VerificaTabelaRotas();  

//Já o metodo Verifica tabela frame, fará a busca de rota para um endereco de 16 pre definido para onde ele queira rotear, CASO SEJA SEU ROETADOR A UTILIZAR ESSE MÉTODO DESMARQUE-O
        } 
        if(contador == 10){
           contador = 0;                                   
        }               
        else{
          contador++;      
        }           
       
    }      
}
}

void VerificaTabelaRotas(){
   //Metodo que irá veriicar na estrutura de dados, as informaçoes para enviar um pacote para um determinado endereco
  
  if(NumeroAddreses.getValueOf(somaAddress16) == 0){
    //Significa que nao ha entrada para aquela rota, entao deve-se realizar uma solicitacao de rotas para o coordenador
    Envia();   
  }else{
    CreateSourceRoute();
 }
  
}


void Mensagem(){
//7E 00 10 10 01 00 13 A2 00 40 A5 9D D6 00 00 00 00 48 49 50

//Este metodo gera uma mensagem para enviar para o coordenador para gerar o pacote route record indicator

Serial.write(0x7e);
Serial.write((byte)0x0);
Serial.write(0x10);
Serial.write(0x10);
Serial.write(0x01);
Serial.write((byte)0x0);
Serial.write(0x13);
Serial.write(0xa2);
Serial.write((byte)0x0);
Serial.write(0x40);
Serial.write(0xa5);
Serial.write(0x9d);
Serial.write(0xd6);
Serial.write((byte)0x0);
Serial.write((byte)0x0);
Serial.write((byte)0x0);
Serial.write((byte)0x0);
Serial.write(0x48);
Serial.write(0x49);
Serial.write(0x50);

}

void VerificaFrame(){
  //Método para a verificação do pacote enviado do coordenador para o roteador que enviou a solicitação de rotas  
  
  
  tamanho = sizeof(frame);
   
  for(int i = 0; i < tamanho; i++){
     if(i == 1){
        numAddresses = frame[i];
     }
     else if(frame[i] == 0){
       continue;      
     }else{
       rota[i] = frame[i];
     }     
   }
 
  
 int a = sizeof(rota);
 
 for(int i = 0; i < a; i++){
   if(i == 2){
     MSB = rota[i];
   }
   if(i == 3){
     LSB = rota[i];
   }
   
   
   if(i == 4){
     MSB16 = rota[i];
   }
   if(i == 5){
     LSB16 = rota[i];
   }
   
 }
  
  somaAddress16 = MSB16 + LSB16;
  Serial.println("VERIFICA FRAME: ");
  Serial.println(numAddresses);
  Serial.println(MSB);
  Serial.println(LSB);
  Serial.println(MSB16);
  Serial.println(LSB16);
  
  //Guardo os valores recebidos en suas respectivas Estruturas de Dados
  
  NumeroAddreses[0](somaAddress16,numAddresses);
  msb[0](somaAddress16,MSB);
  lsb[0](somaAddress16,LSB);
  msb16[0](somaAddress16,MSB16);
  lsb16[0](somaAddress16,LSB16);
  
  CreateSourceRoute();
   
}

void EnviaComando(){
  //Metodo para enviar uma mensagem para um destino com um comando pre definido
  
  
  int Msb = msb16.getValueOf(somaAddress16);
  int Lsb = lsb16.getValueOf(somaAddress16);
  
  Serial.println("EVIA COMANDO: ");
  Serial.println(Msb);
  Serial.println(Lsb);
  
  //int Msb = 0x5c;
  //int Lsb = 0xc0;
  
  //7E 00 0F 10 00 00 13 A2 00 40 A8 36 9E 35 75 00 00 01 D3
  
  Serial.write(0x7e);
  Serial.write((byte)0x0);
  Serial.write(0x0f);
  Serial.write(0x10);
  Serial.write((byte)0x0);
  Serial.write((byte)0x0);
  Serial.write(0x13);
  Serial.write(0xa2);
  Serial.write((byte)0x0);
  Serial.write(0x40);
  Serial.write(0xa8);
  Serial.write(0x36);
  Serial.write(0x9e);
  Serial.write(Msb);
  Serial.write(Lsb);
  Serial.write((byte)0x0);
  Serial.write((byte)0x0);
  Serial.write(0x01);      //COMANDO... ENVIA 1 PARA ACENDER A LED DO DETERMINADO ROTEADOR
  int soma = 0x10 + 0x13 + 0xa2 + 0x40 + 0xa8 + 0x36 + 0x9e + Msb + Lsb + 0x01;
  
  int checksum = (0xff - (soma & 0xff));
  Serial.write(checksum);
  
  delay(3000);
  

  //CASO QUEIRA QUE A LED DO ROTEADOR DESTINO ACENDA E APAGUE DEPOIS, DESCOMENTE ESSA PARTE DE CÓDIGO 			
  //7E 00 0F 10 00 00 13 A2 00 40 A8 36 9E A4 A1 00 00 02 37
  /*
  Serial.write(0x7e);
  Serial.write((byte)0x0);
  Serial.write(0x0f);
  Serial.write(0x10);
  Serial.write((byte)0x0);
  Serial.write((byte)0x0);
  Serial.write(0x13);
  Serial.write(0xa2);
  Serial.write((byte)0x0);
  Serial.write(0x40);
  Serial.write(0xa8);
  Serial.write(0x36);
  Serial.write(0x9e);
  Serial.write(0xa4);
  Serial.write(0xa1);
  Serial.write((byte)0x0);
  Serial.write((byte)0x0);
  Serial.write(0x02);      //COMANDO... ENVIA 1 PARA DESLIGAR A LED DO DETERMINADO ROTEADOR
  Serial.write(0x37);  
  */
}

void InterpretaComando(){
  //Metodo para interpretar um comando enviado por outro roteador, no caso acende ou apaga uma led
  
  if(frame[0] == 1){
    digitalWrite(10,HIGH);
  }else if(frame[0] == 2){
    digitalWrite(10,LOW);
  }  
}


void CreateSourceRoute(){
  //EX: 7E 00 10 21 00 00 13 A2 00 40 A5 9E D5 FF FE 00 01 11 11 B1  
   
  //Cria uma rota com as informações armazenadas na estrutura de dados 
   
  int NumeroAddr = NumeroAddreses.getValueOf(somaAddress16);
  int Msb = msb.getValueOf(somaAddress16);
  int Lsb = lsb.getValueOf(somaAddress16);
  int Msb16 = msb16.getValueOf(somaAddress16);
  int Lsb16 = lsb16.getValueOf(somaAddress16);
  
  Serial.println("CREATE SOURCE ROUTE: ");
  Serial.println(Msb);
  Serial.println(Lsb);
  Serial.println(Msb16);
  Serial.println(Msb16);
  
  //7E 00 0F 10 01 00 13 A2 00 40 A8 36 9E CF 16 00 00 01 97
  
  Serial.write(0x7e);
  Serial.write(0x00);
  Serial.write(0x10);
  Serial.write(0x21);
  Serial.write(0x00);
  Serial.write(0x00);
  Serial.write(0x13);
  Serial.write(0xa2);
  Serial.write(0x00);
  Serial.write(0x40);
  Serial.write(0xa8);
  Serial.write(0x36);
  Serial.write(0x9e);
  Serial.write(Msb16);
  Serial.write(Lsb16);
  Serial.write((byte)0x0);
  Serial.write(NumeroAddr); //NUMERO DE ENDERECOS (SALTOS)
  Serial.write(Msb); // MSB DESTINO
  Serial.write(Lsb); // LSB DESTINO (CASO CONTENHA MAIS VALORES ABAXO ENTAO DEVE TER SEMPRE MSB & LSB)
  //Serial.write(Msb2);
  //Serial.write(Lsb2);
  int soma = 0x21 + 0x13 + 0xa2 + 0x40 + 0xa8 + 0x36 + 0x9e + Msb16 + Lsb16 + NumeroAddr + Msb + Lsb;// + Msb2 + Lsb2;
  int checksum = (0xff - (soma & 0xff));
  Serial.write(checksum);
  
  delay(2000);
  
  EnviaComando();
}


void Envia(){ 
  
//Método que conterá a mensagem que será enviada para o coordenador, em que este deseja enviar mensagens  
//Envia esta mensagem com o endereco de 16 bits para o coordenador para este calcular a rota

//7E 00 12 10 00 00 13 A2 00 40 A5 9D D6 00 00 00 00 4F 4B 35 75 9E

Serial.write(0x7E); // Sync up the start byte
Serial.write((byte)0x0);
Serial.write(0x12); // Length LSB
Serial.write(0x10); // 0x17 is the frame ID for sending an AT command
Serial.write((byte)0x0); // Frame ID (no reply needed)
Serial.write((byte)0x0);
Serial.write(0x13);
Serial.write(0xA2);
Serial.write((byte)00);
Serial.write(0x40);
Serial.write(0xA5);
Serial.write(0x9D);
Serial.write(0xD6);
Serial.write((byte)0x0); // Destination Network
Serial.write((byte)0x0); // (Set to 0xFFFE if unknown)
Serial.write((byte)0x0);
Serial.write((byte)0x0);
Serial.write(0x4F);
Serial.write(0x4B);
Serial.write(0xA8);
Serial.write(0x9E);
Serial.write(0x02);
}


