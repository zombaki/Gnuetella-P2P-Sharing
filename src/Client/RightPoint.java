package Client;

public class RightPoint {
	public static void main(String[] args) {
		for (int i =1 ; i<=100;i++){
			
			if(i%3==0 &&i%5==0)
				System.out.print("rightpoint");
			else if(i%3==0)
				System.out.print("right");
			else if (i%5==0)
				System.out.print("point");
			else
				System.out.print(i);
			System.out.print("\n");
		}
	}
}
