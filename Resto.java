package test_project;


import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;

public class Test extends Applet {
	
	
	// La classe  
	final static byte RESTO_CLA = (byte)0x80 ;
	
	//Les instructions 
    final static byte VERIFY = (byte) 0x20;
    final static byte RECHARGE = (byte)0x30;
    final static byte DEBIT =(byte)0x40;
    final static byte CONSULTATION =(byte)0x50;
    final static byte ADMINISTRATION=(byte)0x60;
    
    //Les constantes
    final static short maxBalance = 0x7FFF;
    final static byte maxTransactionMontant = 127;
    final static byte nbEssaisPIN = (byte)0x03;
    final static byte maxTaillePIN = (byte)0x08;
    
    //Les réponses
    final static short SW_AUTHENTIFICATION_ECHOUE =  0x6300;
    final static short SW_PIN_AUTHENTIFICATION_REQUISE = 0x6301;
    final static short SW_PIN_ESSAI_NULL = 0x6302;
    final static short SW_INVALID_TRANSACTION_MONTANT = 0x6A83;
    final static short SW_MAXIMUM_BALANCE_DEPASSER = 0x6A84;
    final static short SW_NEGATIF_BALANCE = 0x6A85;
    
    //Le PIN
    OwnerPIN pin;
    
    //La balance
    short balance ;
    
    //Informations de personnalisation
    byte [] nom,prenom,matricule,filiere,date ;
    
   
   
    
    private Test (byte[] bArray,short bOffset,byte bLength)  // Constructeur
    {
        pin = new OwnerPIN(nbEssaisPIN,maxTaillePIN);
        
        byte PINLong = bArray[bOffset]; // PIN taille
        pin.update(bArray, (short)(bOffset+1), PINLong); //Initialisation de PIN

        
        bOffset = (short) (bOffset+PINLong+1);
        byte matLong = bArray[bOffset]; // Longueur matricule 
        matricule = new byte[(short)matLong];
        Util.arrayCopy(bArray, (short)(bOffset +1),matricule,(short)0,matLong); //Initialisation de matricule

        
        bOffset = (short) (bOffset+matLong+1);
        byte nomLong = bArray[bOffset]; // Longueur nom 
        nom = new byte[(short)nomLong];
        Util.arrayCopy(bArray, (short)(bOffset +1),nom,(short)0,nomLong); //Initialisaton du nom
        
        
        
        bOffset = (short) (bOffset+nomLong+1);
        byte prenomLong = bArray[bOffset]; // Longueur prénom 
        prenom = new byte[(short)prenomLong]; 
        Util.arrayCopy(bArray, (short)(bOffset +1),prenom,(short)0,prenomLong); //Initialisation du prénom

     
        
        bOffset = (short) (bOffset+prenomLong+1); 
        byte filiereLong = bArray[bOffset]; // Longueur filière
        filiere = new byte[(short)filiereLong];
        Util.arrayCopy(bArray, (short)(bOffset +1),filiere,(short)0,filiereLong); //Initialisation de la filière
        
        
        bOffset = (short) (bOffset+filiereLong+1);
        date = new byte[(short)3];
        Util.arrayCopy(bArray, (short)(bOffset +1),date,(short)0,3); // Initialisation de la date d'expiration
        

        register(); //Enregistrement de l'applet

    }//Fin du constructeur 
    
    public static void install(byte[] bArray, short bOffset, byte bLength) //Installation de l'applet
    {
    	
        new Test(bArray, bOffset, bLength);
    } 

    public boolean select() 
    {
     
        if ( pin.getTriesRemaining() == 0 ) //Refuser la sélection si le nombre d'essais restants == 0
           return false;
        
        return true;     
    }

    
    public void deselect() 
    {
        
        pin.reset(); //Réinitialisation des paramétres du PIN
    }

    
    public void process(APDU apdu)
    {
    	
    	
    	byte [] buffer = apdu.getBuffer();
    	
    	 if (apdu.isISOInterindustryCLA())
         {
             if (buffer[ISO7816.OFFSET_INS] == (byte)(0xA4))
             {
                 return;
             } 
             else 
             {
                 ISOException.throwIt (ISO7816.SW_CLA_NOT_SUPPORTED);
             }
         }
    	 
         if (buffer[ISO7816.OFFSET_CLA] != RESTO_CLA)
         {
        	 ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
         }
         
         if ( pin.getTriesRemaining() == 0  )
    	 {
    		 ISOException.throwIt(SW_PIN_ESSAI_NULL);
    	 }
         
         if(!pin.isValidated() && buffer[ISO7816.OFFSET_INS] != VERIFY ) 
         {
    		 ISOException.throwIt(SW_PIN_AUTHENTIFICATION_REQUISE);
         }
         
         
         
         switch(buffer[ISO7816.OFFSET_INS])
         {
         case ADMINISTRATION :
         	switch(buffer[ISO7816.OFFSET_P1])
         	{
         		case 0x00 :
         			changepin(apdu);
         			return;
         		case 0x01 :
         			changedate(apdu);
         			return;
         		 default :
             ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
         	}
         case CONSULTATION :
        	 switch(buffer[ISO7816.OFFSET_P1])
        	 {
        	 case 0x00 :
        		 getBalance(apdu);
        		 return;
        	 case 0x01:
        		 getInfo(apdu,matricule);
        		 return;
        	 case 0x02 :
        		 getInfo(apdu,nom);
        		 return;
        	 case 0x03 :
        		 getInfo(apdu,prenom);
        		 return;
        	 case 0x04 :
        		 getInfo(apdu,filiere);
        		 return;
        	 case 0x05 :
        		 getInfo(apdu,date);
        		 return;
        	 default :
             ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);

        	 }
        	 return;
         case DEBIT :
        	 debit(apdu);
        	 return;
         case RECHARGE :
        	 recharge(apdu);
        	 return;
         case VERIFY :
        	 verify(apdu);
        	 return;
         default :
        	 ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
         }

    } //Fin de la méthode process 

    public void recharge(APDU apdu)
    {
    	
    	
    	byte [] buffer = apdu.getBuffer();
    	
    	byte tailleMontant = buffer[ISO7816.OFFSET_LC]; //En byte
    	byte nbByteLus = (byte)(apdu.setIncomingAndReceive()); //En byte
    	
    	if((tailleMontant != 1) || (nbByteLus != 1)) // Vérifier que tailleMontant = nbByteLus = 1
    	{ 
    		ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    	}
    	
    	byte rechargeMontant = buffer[ISO7816.OFFSET_CDATA]; //La valeur à recharger 
    	
    	if((rechargeMontant > maxTransactionMontant) || (rechargeMontant < 0)) //Vérifier que rechargeMontant< 127 et > 0
    	{
    		ISOException.throwIt(SW_INVALID_TRANSACTION_MONTANT);
    	}
    	
    	if((short)(rechargeMontant + balance ) > (short)maxBalance) //vérifier que la nouvelle balance est < a maxBalance
    	{
    		ISOException.throwIt(SW_MAXIMUM_BALANCE_DEPASSER);
    	}
    	 
    	balance = (short)(balance + rechargeMontant); // Valider la nouvelle balance
    	
    }//Fin de la méthode recharge()
    
    private void debit(APDU apdu) 
    {
    	
    	byte [] buffer = apdu.getBuffer();

    	byte tailleMontant = buffer[ISO7816.OFFSET_LC]; //En byte
    	byte nbByteLus = (byte)(apdu.setIncomingAndReceive()); //En byte
    	
    	if((tailleMontant != 1) || (nbByteLus != 1)) // Vérifier que tailleMontant = nbByteLus = 1
    	{ 
    		ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    	}
    	
    	byte debiteMontant = buffer[ISO7816.OFFSET_CDATA];
    			
    	if((debiteMontant > maxTransactionMontant) || (debiteMontant < 0))
    	{
    		ISOException.throwIt(SW_INVALID_TRANSACTION_MONTANT);
    	}
    	
    	if ( (short)( balance - debiteMontant ) < (short)0 )
    	{
            ISOException.throwIt(SW_NEGATIF_BALANCE);
    	}
    	
        balance = (short) (balance - debiteMontant);

    	
    }// Fin de la méthode debit
    
    
    public void getBalance(APDU apdu)
    {
        byte[] buffer = apdu.getBuffer();
        
        short le = apdu.setOutgoing();
        
        if ( le < 2 )
        {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        }
        
        apdu.setOutgoingLength((byte)2);
        
        buffer[0] = (byte)(balance >> 8);
        buffer[1] = (byte)(balance & 0xFF);
        
        apdu.sendBytes((short)0, (short)2);
   }//Fin de la méthode getBalance
    
    private void getInfo(APDU apdu, byte [] tab)
    {
    	byte [] buffer = apdu.getBuffer();
    	Util.arrayCopyNonAtomic(tab, (short)0, buffer, (short)0,(short)(tab.length));
		apdu.setOutgoingAndSend((short)0,(short)(tab.length));

    	
    }//Fin de la méthode getInfo
     
    private void verify(APDU apdu) 
    {
        byte[] buffer = apdu.getBuffer();
        
        byte nbByteLus = (byte)(apdu.setIncomingAndReceive());
        
        if ( pin.check(buffer, ISO7816.OFFSET_CDATA,nbByteLus) == false )
        {
            ISOException.throwIt(SW_AUTHENTIFICATION_ECHOUE);

        }
    }//Fin de la méthode verify
    
    private void changepin(APDU apdu)
    {
    	byte[] buffer = apdu.getBuffer();
    	byte len = (byte)apdu.setIncomingAndReceive();
		
		pin.update(buffer, ISO7816.OFFSET_CDATA, len);
			
		pin.check(buffer, ISO7816.OFFSET_CDATA, len);
    }//Fin de la méthode changepin
    
    private void changedate(APDU apdu)
    {
    	byte[] buffer = apdu.getBuffer();
    	byte len = (byte)apdu.setIncomingAndReceive();
    	Util.arrayCopy(buffer, (short)0,date,(short)0,3);
    }//Fin de la méthode changedate
    
}
