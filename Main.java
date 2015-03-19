package pfeClient;

import java.util.Scanner;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadT1Client;
import com.sun.javacard.apduio.CadTransportException;

public class Main {

	//la classe
	final static byte RESTO_CLA = (byte)0x80 ; 
	
	//les instructions 
    final static byte VERIFY = (byte) 0x20;
    final static byte RECHARGE = (byte)0x30;
    final static byte DEBITE =(byte)0x40;
    final static byte CONSULTATION =(byte)0x50;
    final static byte ADMINISTRATION=(byte)0x60;

    
    //méthode pour convertire un tableau de byte en String 
    private static String toString(byte [] tab) 
	{
		String resultat = "" ;
		for(int i = 0; i< tab.length;i++)
		{
			resultat = resultat+tab[i];
		}
		return resultat ;
	} //fin da la méthode toString(byte [] tab)
    
    
    //méthode pour convertire un tableau de char en String 
    
    private static String toString(char [] tab) //Overload
   	{
   		String resultat = "" ;
   		for(int i = 0; i< tab.length;i++)
   		{
   			resultat = resultat+tab[i];
   		}
   		return resultat ;
   	} //fin da la méthode toString(char [] tab)
    
    
    //méthode pour convertire un tableau de byte en tableau de char ;
    private static char [] byteToChar(byte [] byteArray)
    {
    	char [] resultat = new char[byteArray.length];
    	for(int i = 0; i < byteArray.length ; i++)
    	{
    		resultat[i] = (char)byteArray[i];
    	}
    	return resultat;
    	
    }
    
    //méthode de traitement des réponses 
    
    private static void evalSW(Apdu apdu)
    {
    	int sw = apdu.getStatus();
    	switch(sw)
    	{
    	case 0x9000: System.out.println("Commande réussite");break;
    	case 0x6300: System.out.println("authentification échouée");break;
    	case 0x6301: System.out.println("authentification requise");break;
    	case 0x6302: System.out.println("Nombre d'essai de PIN nulle");break;
    	case 0x6A83: System.out.println("Motant de transaction invalide");break;
    	case 0x6A84: System.out.println("La balance maximal dépassé");break;
    	case 0x6A85: System.out.println("Balance est insuffisante");break;
    	case 0x6A86: System.out.println("Paramètre non valide");break;
    	case 0x6E00: System.out.println("La classe de la commande est non supporté");break;
    	case 0x6D00: System.out.println("Instruction non supporté");break;
    	case 0x6700: System.out.println("La longueur des données est invalide");break;

    	
    	}
    }
    

    
    //méthode d'authenitifcation (build verify APDU command)
    
    private static void verify(Apdu apdu,CadT1Client cad,Scanner sc) throws CadTransportException,IOException
    {
    	apdu.command[Apdu.INS] = Main.VERIFY;
		apdu.command[Apdu.P1] = 0x00;
		apdu.command[Apdu.P2] = 0x00;
		byte [] pin = new byte[5];
		System.out.println("Entrez le PIN : ");
		for(int i = 0;i<5;i++)
		{
			pin[i] = sc.nextByte();
		}
		apdu.setDataIn(pin);
		cad.exchangeApdu(apdu);
		evalSW(apdu);
    }//fin de la méthode verify
    
    
    private static byte [] consultation(byte p1, Apdu apdu, CadT1Client cad) throws CadTransportException,IOException
    {
    	apdu.command[Apdu.INS] = Main.CONSULTATION;
		apdu.command[Apdu.P1] = p1;
		apdu.command[Apdu.P2] = 0x00;
		if(p1 ==(byte)0x00 ) apdu.Le = 2;
		cad.exchangeApdu(apdu);
		evalSW(apdu);
		if (apdu.getStatus() != 0x9000) 
		{
			return null;
		}
		else
		{	byte [] Rep = new byte[apdu.dataOut.length];
			Rep = apdu.dataOut ;
			return Rep;
		}
    }//fin de la méthode consultation
    
    private static void recharge(Apdu apdu,CadT1Client cad,Scanner sc) throws CadTransportException,IOException
    {
    	byte [] montant = new byte[1]; 
    	System.out.println("Entrez le montant : ");
    	montant[0] =  sc.nextByte() ;

    	apdu.command[Apdu.INS] = Main.RECHARGE;
		apdu.command[Apdu.P1] = 0x00;
		apdu.command[Apdu.P2] = 0x00;
		apdu.setDataIn(montant);
		cad.exchangeApdu(apdu);
		evalSW(apdu);
		
    }// fin de la méthode recharge
    
    
    
    private static void debite(Apdu apdu,CadT1Client cad,Scanner sc) throws CadTransportException,IOException
    {
    	byte [] montant = new byte[1]; 
    	System.out.println("Entrez le montant : ");
    	montant[0] =  sc.nextByte() ;

    	apdu.command[Apdu.INS] = Main.DEBITE;
		apdu.command[Apdu.P1] = 0x00;
		apdu.command[Apdu.P2] = 0x00;
		apdu.setDataIn(montant);
		cad.exchangeApdu(apdu);
		evalSW(apdu);

		
    }// fin de la méthode debite
    
    
    private static void update(byte p1,Apdu apdu,CadT1Client cad,Scanner sc) throws CadTransportException,IOException
    {
    	if(p1 == 0x00)
    	{
    		apdu.command[Apdu.INS] = Main.ADMINISTRATION;
    		apdu.command[Apdu.P1] = p1;
    		apdu.command[Apdu.P2] = 0x00;
    		byte [] pin = new byte[5];
    		System.out.println("Entrez le nouveau PIN : ");
    		for(int i = 0;i<5;i++)
    		{
    			pin[i] = sc.nextByte();
    		}
    		apdu.setDataIn(pin);
    		cad.exchangeApdu(apdu);
    		evalSW(apdu);
    		
    	}
    	else if(p1 == 0x01)
    	{
    		apdu.command[Apdu.INS] = Main.ADMINISTRATION;
    		apdu.command[Apdu.P1] = p1;
    		apdu.command[Apdu.P2] = 0x00;
    		byte [] date = new byte[3];
    		System.out.println("Entrez la nouvelle date : ");
    		for(int i = 0;i<3;i++)
    		{
    			date[i] = sc.nextByte();
    		}
    		apdu.setDataIn(date);
    		cad.exchangeApdu(apdu);
    		evalSW(apdu);

    	}
    	
    	else System.out.println("Pramètre invalide");
    }
  
    
    
    

	public static void main(String[] args) throws Exception {
		/* Connexion a la Javacard */
		
		CadT1Client cad;
		Socket sckCarte;
		
		try {
			sckCarte = new Socket("localhost", 9025);
			sckCarte.setTcpNoDelay(true);
			BufferedInputStream input = new BufferedInputStream(sckCarte.getInputStream());
			BufferedOutputStream output = new BufferedOutputStream(sckCarte.getOutputStream());
			cad = new CadT1Client(input, output);
			}
		catch (Exception e) 
		 	{
				System.out.println("Erreur : impossible de se connecter a la Javacard");
				return;
		 	}		
		
		/* Mise sous tension de la carte */
		
		try {
			cad.powerUp();
			}
		catch (Exception e) 
			{
			System.out.println("Erreur lors de l'envoi de la commande Powerup a la Javacard");
			return;
			}
		
		/*installation de l'applet */
	
		
		/* envoi des information de l'initialisation */
		
		
		Apdu installApdu = new Apdu();
		
		installApdu.command[Apdu.CLA] = (byte)0x80 ;
		installApdu.command[Apdu.INS] =  (byte) 0xB8;
		installApdu.command[Apdu.P1] = 0x00;
		installApdu.command[Apdu.P2] = 0x00;
		
		byte [] installPram = 
			    {
				(byte)0xb, (byte)0x01, (byte)0x02,(byte) 0x03, (byte)0x04,(byte) 0x05,(byte) 0x06, (byte)0x07,(byte) 0x08, (byte)0x09,(byte) 0x00,(byte) 0x00,
				(byte) 0x30, (byte)0x05,(byte) 0x01, (byte)0x02, (byte)0x03,(byte) 0x04,(byte) 0x05,
				(byte)0x0C, (byte) 2,(byte) 0,(byte) 1,(byte) 2,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 6,(byte) 4,(byte) 5,(byte) 5,
				(byte) 0x09,(byte) 'B',(byte) 'E',(byte) 'K',(byte) 'K',(byte) 'O',(byte) 'U',(byte) 'C',(byte) 'H',(byte) 'E',
				(byte)0x07,(byte) 'O',(byte) 'U',(byte) 'S',(byte) 'S',(byte) 'A',(byte) 'M',(byte) 'A',
				(byte)0x06, (byte)'L', (byte)'I',(byte) 'C',(byte) '.',(byte) 'M',(byte) 'I',
				(byte)0x03,(byte) 13,(byte) 03,(byte) 15
				};
		
		installApdu.setDataIn(installPram);
		cad.exchangeApdu(installApdu);
		
		if (installApdu.getStatus() != 0x9000) 
		{
			System.out.println("Erreur lors de l'installation de l'applet " + installApdu.getStatus());
			System.exit(1);
		} 
		
		
		
		

		
		/* Sélection de l'applet */
		
		Apdu selectApdu = new Apdu();
		selectApdu.command[Apdu.CLA] = 0x00;
		selectApdu.command[Apdu.INS] = (byte) 0xA4;
		selectApdu.command[Apdu.P1] = 0x04;
		selectApdu.command[Apdu.P2] = 0x00;
		byte[] appletAID = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x00, 0x00 };
		selectApdu.setDataIn(appletAID);
		cad.exchangeApdu(selectApdu);
		
		if (selectApdu.getStatus() != 0x9000) 
		{
			System.out.println("Erreur lors de la sélection de l'applet");
			System.exit(1);
		}
		
		
		/* Menu principal */
		Scanner sc = new Scanner(System.in);
		boolean fin = false;
		while (!fin)
		{
			System.out.println();
			System.out.println("Application client Resto Javacard");
			System.out.println("----------------------------");
			System.out.println();
			System.out.println("A - authentification");
			System.out.println("B - consultation de balance");
			System.out.println("C - consultation de matricule");
			System.out.println("D - consultation de nom");
			System.out.println("E - consultation de prenom");
			System.out.println("F - consultation de filiere");
			System.out.println("G - consultation de date");
			System.out.println("H - Recharger la balance");
			System.out.println("I - Débiter la balnce");
			System.out.println("J - Modifier le PIN");
			System.out.println("K - Modifier la date");
			System.out.println("L - Quiter ");
			System.out.println("*************************");
			System.out.println("Votre choix ??");

			String choix = sc.next();
			choix = choix.toUpperCase();
			int c = choix.charAt(0);
			
			while (!(c >= 'A' && c <= 'L')) 
			{
				System.out.println("Choix invalide");
				choix = sc.next();
				choix = choix.toUpperCase();
			    c = choix.charAt(0);
			}
			
			
		  Apdu	apdu = new Apdu();
			apdu.command[Apdu.CLA] = Main.RESTO_CLA;
			
			switch(c)
			{
			case 'A' :
				verify(apdu,cad,sc);
				break;
				
			case 'B' :
			byte [] RepBalance =consultation((byte)0x00,apdu,cad);
			if(RepBalance != null){
			String Balance = toString(RepBalance);
			System.out.println("La balance est : " + Balance);	}
				break;
				
			case 'C' :
				byte [] RepMat = consultation((byte)0x01,apdu,cad);
				if(RepMat != null){
				String matricule = toString(RepMat);
				System.out.println("le matricule est " + matricule );}
				break;
				
			case 'D' :
				byte [] RepNom = consultation((byte)0x02,apdu,cad);
				if(RepNom != null){
				char [] CharRepNom = byteToChar(RepNom);
				String nom = toString(CharRepNom);
				System.out.println("Le nom est " + nom);}
				break;
			
			case 'E' :
				byte [] RepPrenom = consultation((byte)0x03,apdu,cad);
				if(RepPrenom != null){
				char [] CharRepPrenom = byteToChar(RepPrenom);
				String prenom = toString(CharRepPrenom);
				System.out.println("Le prenom est " + prenom);}
				break;
				
			case 'F' :
				byte [] RepFiliere = consultation((byte)0x04,apdu,cad);
				if(RepFiliere != null){
				char [] CharRepFiliere = byteToChar(RepFiliere);
				String filiere = toString(CharRepFiliere);
				System.out.println("La filiere est " + filiere);}
				break;
			case 'G' :
				byte [] RepDate = consultation((byte)0x05,apdu,cad);
				if(RepDate != null){
				String date = toString(RepDate);
				System.out.println("La date est " + date);}
				break;
				
			case  'H' :
				recharge(apdu,cad,sc);
				break;
			case 'I' :
				debite(apdu,cad,sc);
				break;
				
			case 'J' :
				update((byte)0x00,apdu,cad,sc);
				break;
				
			case 'K' :
				update((byte)0x01,apdu,cad,sc);
				break;
				
			case 'L' : 
				fin = true ;
				cad.powerDown();

				break;
			
				
			}

		
		}


	}

}
