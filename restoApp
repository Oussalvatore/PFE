package test_project;


import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;

public class Test extends Applet {
	
	
	// la classe  
	final static byte RESTO_CLA = (byte)0x80 ;
	
	//les instructions 
    final static byte VERIFY = (byte) 0x20;
    final static byte RECHARGE = (byte)0x30;
    final static byte DEBITE =(byte)0x40;
    final static byte CONSULTATION =(byte)0x50;
    
    //les constants
    final static short maxBalance = 0x7FFF;
    final static byte maxTransactionMontant = 127;
    final static byte nbEssaisPIN = (byte)0x03;
    final static byte maxTaillePIN = (byte)0x08;
    
    //les réponses
    final static short SW_AUTHENTIFICATION_ECHOUE =  0x6300;
    final static short SW_PIN_AUTHENTIFICATION_REQUISE = 0x6301;
    final static short SW_PIN_ESSAI_NULL = 0x6302;
    final static short SW_INVALID_TRANSACTION_MONTANT = 0x6A83;
    final static short SW_MAXIMUM_BALANCE_DEPASSER = 0x6A84;
    final static short SW_NEGATIF_BALANCE = 0x6A85;
    
    //le PIN
    OwnerPIN pin;
    
    //la balance
    short balance ;
    
    //informations de personalisation
    byte [] nom,prenom,matricule,filiere,date ;
    
   
   
    
    private Test (byte[] bArray,short bOffset,byte bLength)  // Constructeur
    {
        pin = new OwnerPIN(nbEssaisPIN,maxTaillePIN);
        
        byte PINLong = bArray[bOffset]; // PIN taille
        pin.update(bArray, (short)(bOffset+1), PINLong); //initialisation de PIN

        
        bOffset = (short) (bOffset+PINLong+1);
        byte matLong = bArray[bOffset]; // Matricule longueur
        matricule = new byte[(short)matLong];
        Util.arrayCopy(bArray, (short)(bOffset +1),matricule,(short)0,matLong); //initialisation de matricule

        
        bOffset = (short) (bOffset+matLong+1);
        byte nomLong = bArray[bOffset]; // nom longueur
        nom = new byte[(short)nomLong];
        Util.arrayCopy(bArray, (short)(bOffset +1),nom,(short)0,nomLong); //initialisaton de nom
        
        
        
        bOffset = (short) (bOffset+nomLong+1);
        byte prenomLong = bArray[bOffset]; // prenom longueur
        prenom = new byte[(short)prenomLong]; 
        Util.arrayCopy(bArray, (short)(bOffset +1),prenom,(short)0,prenomLong); //initialisation  de prénom

     
        
        bOffset = (short) (bOffset+prenomLong+1); 
        byte filiereLong = bArray[bOffset]; // filiere longueur
        filiere = new byte[(short)filiereLong];
        Util.arrayCopy(bArray, (short)(bOffset +1),filiere,(short)0,filiereLong); //initialisation de la filiere
        
        
        bOffset = (short) (bOffset+filiereLong+1); 
        byte dateLong = bArray[bOffset]; // filiere longueur
        date = new byte[(short)dateLong];
        Util.arrayCopy(bArray, (short)(bOffset +1),date,(short)0,dateLong); // initialisation de la date d'experation
        

        register(); //enregistrement de l'applet

    }//fin de constructeur 
    
    public static void install(byte[] bArray, short bOffset, byte bLength) //installation de l'applet
    {
    	
        new Test(bArray, bOffset, bLength);
    } 

    public boolean select() 
    {
     
        if ( pin.getTriesRemaining() == 0 ) //réfuser la sélection si le nombre des essais restants == 0
           return false;
        
        return true;     
    }

    
    public void deselect() 
    {
        
        pin.reset(); //réinitialisation des paramétres de PIN
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
         case CONSULTATION :
        	 switch(buffer[ISO7816.OFFSET_P1])
        	 {
        	 case 0x00 :
        		 getBalance(apdu);
        		 return;
        	 case 0x01:
        		 getMatricule(apdu);
        		 return;
        	 case 0x02 :
        		 getNom(apdu);
        		 return;
        	 case 0x03 :
        		 getPrenom(apdu);
        		 return;
        	 case 0x04 :
        		 getFiliere(apdu);
        		 return;
        	 case 0x05 :
        		 getDate(apdu);
        		 return;
        	 default :
             ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);

        	 }
        	 return;
         case DEBITE :
        	 debite(apdu);
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

    } //fin de la méthode processe 

    public void recharge(APDU apdu)
    {
    	
    	
    	byte [] buffer = apdu.getBuffer();
    	
    	byte tailleMontant = buffer[ISO7816.OFFSET_LC]; //en byte
    	byte nbByteLus = (byte)(apdu.setIncomingAndReceive()); //en byte
    	
    	if((tailleMontant != 1) || (nbByteLus != 1)) // vérifier que tailleMontant = nbByteLus = 1
    	{ 
    		ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    	}
    	
    	byte rechargeMontant = buffer[ISO7816.OFFSET_CDATA]; //la valeur a rechargé 
    	
    	if((rechargeMontant > maxTransactionMontant) || (rechargeMontant < 0)) //vérifier que rechargeMontant< 127 et > 0
    	{
    		ISOException.throwIt(SW_INVALID_TRANSACTION_MONTANT);
    	}
    	
    	if((short)(rechargeMontant + balance ) > (short)maxBalance) //vérifier que la nouvelle balance est < a maxBalance
    	{
    		ISOException.throwIt(SW_MAXIMUM_BALANCE_DEPASSER);
    	}
    	 
    	balance = (short)(balance + rechargeMontant); // valider la nouvelle balance
    	
    }//fin de la méthode recharge()
    
    private void debite(APDU apdu) 
    {
    	
    	byte [] buffer = apdu.getBuffer();

    	byte tailleMontant = buffer[ISO7816.OFFSET_LC]; //en byte
    	byte nbByteLus = (byte)(apdu.setIncomingAndReceive()); //en byte
    	
    	if((tailleMontant != 1) || (nbByteLus != 1)) // vérifier que tailleMontant = nbByteLus = 1
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

    	
    }// fin de la méthode debite
    
    
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
   }//fin de la méthode getBalance
    
    private void getMatricule(APDU apdu)
    {
    	byte [] buffer = apdu.getBuffer();
    	Util.arrayCopyNonAtomic(matricule, (short)0, buffer, (short)0,(short)(matricule.length));
		apdu.setOutgoingAndSend((short)0,(short)(matricule.length));

    	
    }//fin de la méthode getMatricule
    
    
    private void getNom(APDU apdu)
    {
    	byte [] buffer = apdu.getBuffer();
    	Util.arrayCopyNonAtomic(nom, (short)0, buffer, (short)0,(short)(nom.length));
		apdu.setOutgoingAndSend((short)0,(short)(nom.length));
		
    }//fin de la méthode getNom
    
    private void getPrenom(APDU apdu)
    {
    	byte [] buffer = apdu.getBuffer();
    	Util.arrayCopyNonAtomic(prenom, (short)0, buffer, (short)0,(short)(prenom.length));
		apdu.setOutgoingAndSend((short)0,(short)(prenom.length));
    	
    }//fin de la méthode getPrenom
    
    private void getFiliere(APDU apdu)
    {
    	byte [] buffer = apdu.getBuffer();
    	Util.arrayCopyNonAtomic(filiere, (short)0, buffer, (short)0,(short)(filiere.length));
		apdu.setOutgoingAndSend((short)0,(short)(filiere.length));
		
    }//fin de la méthode getFiliere
    
    
    private void getDate(APDU apdu)
    {
    	byte [] buffer = apdu.getBuffer();
    	Util.arrayCopyNonAtomic(date, (short)0, buffer, (short)0,(short)(date.length));
		apdu.setOutgoingAndSend((short)0,(short)(date.length));
		
    }//fin de la méthode getFiliere
    
    
    private void verify(APDU apdu) 
    {
        byte[] buffer = apdu.getBuffer();
        
        byte nbByteLus = (byte)(apdu.setIncomingAndReceive());
        
        if ( pin.check(buffer, ISO7816.OFFSET_CDATA,nbByteLus) == false )
        {
            ISOException.throwIt(SW_AUTHENTIFICATION_ECHOUE);

        }
    }//fin de la méthode verify
    
    
    
    
    
   

    
	
}
