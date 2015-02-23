#include <XBee.h>
#include <HashMap.h>

int teste = 0;
int analogReading;
XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
ZBExpRxResponse rx = ZBExpRxResponse();

const byte HASH_SIZE = 5; 

HashType<int*,int*> hashRawArray[HASH_SIZE]; 
HashMap<int*,int*> hashMap = HashMap<int*,int*>( hashRawArray , HASH_SIZE );
int dados[50];
int frame[50];

void setup(){
Serial.begin(9600);
xbee.begin(Serial);
}

void loop(){

  Recebe();

}



void Recebe(){
String teste[40];
byte t[40];

xbee.readPacket();
    
if (xbee.getResponse().isAvailable()) {    
      if (xbee.getResponse().getApiId() == ZB_EXPLICIT_RX_RESPONSE) {
          xbee.getResponse().getZBExpRxResponse(rx);
          
        for (int i = 0; i < rx.getDataLength(); i++) {                    
          dados[i] = rx.getData()[i];
          teste[i] = String(dados[i], HEX);
          
          Serial.println(teste[i]);          
        }
          Serial.println("-------TESTE1---------");
          
        for (int i = 0; i < rx.getDataLength(); i++) {
          t[i] = rx.getData()[i];
          //VerificaFrame(t);
          Serial.println(t[i]);
        }
        VerificaFrame(t);
      }else{ //Caso contrário a mensagem recebida foi um many to one request indicator, se for esse mesmo então envio uma mensagem pre selecionada 
        Envia();
      }
}
}

void VerificaFrame(byte data[]){
  
 for (int i = 0; i < sizeof(10); i++){
      if(data[i] == 0){ //é o primeiro valor do campo de dados ele diz se é dados que contenham modificações
          Serial.println("NÃO HA MODIFICACOES...");
      }else{
        ArmazenaInformacoes(data[]);
      } 
 } 
}

void ArmazenaInformacoes(){
  int parte1[10];
  int parte2[50];
  
  for (int i = 0; i < sizeof(9); i++){
    while(i > 1 && i <= 9){
      parte1[i] = dados[i];
    }    
  }
  
  for (int i = 10; i < sizeof(50); i++){
    
  }
  
  
}

void Envia(){
  
  //Envia uma mensagem de solicitação de rota para o SDNCoordenator
  
//7E 00 10 10 00 00 13 A2 00 40 A5 9E D5 FF FE 00 00 4F 49 4D
  
Serial.write(0x7E); // Sync up the start byte
Serial.write((byte)0x0);
Serial.write(0x10); // Length LSB
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
Serial.write(0xFF); // Destination Network
Serial.write(0xFE); // (Set to 0xFFFE if unknown)
Serial.write((byte)0x0);
Serial.write((byte)0x0);
Serial.write(0x4F);
Serial.write(0x49);
//long chexsum = 0x20 + 0x13 + 0xA2 + 0x40 + 0xA5 + 0x9D + 0xD6 +0xFF + 0xFE + 0x4F + 0x49;
Serial.write(0x4D); 

Recebe(); //Após o envio da mensagem entra no método recebe() para receber a mensagem de rotas
}


