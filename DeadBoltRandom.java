import java.security.SecureRandom;

public class DeadBoltRandom extends SecureRandom{
    byte[] EntropicData;
    
    public DeadBoltRandom (byte[] ExtraEntropy){
    super();
    EntropicData = ExtraEntropy;
    }
    public DeadBoltRandom (byte[] ExtraEntropy, byte[] Seed) {
    super(Seed);
    EntropicData = ExtraEntropy; 
    }
    @Override
    public void nextBytes(byte[] empty){
    super.nextBytes(empty);
    int size = EntropicData.length;
    if (size <=0){
    return;
    }
    int i = 0;
    while ( i < empty.length){
    for (int x = 0; x < empty.length && i < empty.length; x++){
        
    empty[i] = (byte)(0xff &(int)(empty[i] ^ EntropicData[x]));
    i++;
    }
    return;
    }
    
    }
    
    
}
