package komorebi.projsoul.items;

//Represents Pants armor
public class Pants extends Armor 
{

	public Pants(String name, int salePrice, int quantity, int defense, int durability, String description) 
	{
		super(name, salePrice, quantity, defense, durability, description);
		
	}
	
	//Might need different code for these methods (unsure as of right now)
	public void equip() 
	{
		super.equip();
					
	}

	public void unequip() 
	{
		super.unequip();
			
	}

}
