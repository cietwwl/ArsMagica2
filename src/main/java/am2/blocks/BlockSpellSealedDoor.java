package am2.blocks;

import am2.AMCore;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.blocks.tileentities.TileEntitySpellSealedDoor;
import am2.guis.ArsMagicaGuiIdList;
import am2.items.ItemsCommonProxy;
import am2.texture.ResourceManager;
import am2.utility.KeystoneUtilities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class BlockSpellSealedDoor extends BlockDoor implements ITileEntityProvider{
	protected BlockSpellSealedDoor(){
		super(Material.wood);
		this.setHardness(2.5f);
		this.setResistance(2.0f);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.getBlockState(pos.down()).getBlock() == BlocksCommonProxy.spellSealedDoor)
			y--;

		TileEntity te = world.getTileEntity(pos);

		player.swingItem();

		if (!world.isRemote){
			if (KeystoneUtilities.HandleKeystoneRecovery(player, (IKeystoneLockable)te))
				return true;

			if (KeystoneUtilities.instance.canPlayerAccess((IKeystoneLockable)te, player, KeystoneAccessType.USE)){
				if (player.isSneaking()){
					FMLNetworkHandler.openGui(player, AMCore.instance, ArsMagicaGuiIdList.GUI_SPELL_SEALED_DOOR, world, pos.getX(), pos.getY(), pos.getZ());
				}else{
					return false;
				}
			}
		}

		return false;
	}


	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (world.isRemote){
			super.breakBlock(world, pos, state);
			return;
		}

		if (world.getBlockState(i, j - 1, k).getBlock() == BlocksCommonProxy.spellSealedDoor)
			j--;

		TileEntitySpellSealedDoor door = (TileEntitySpellSealedDoor)world.getTileEntity(i, j, k);
		if (door == null) return;
		ItemStack itemstack = door.getStackInSlot(3);
		if (itemstack == null){
			return;
		}
		float f = world.rand.nextFloat() * 0.8F + 0.1F;
		float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
		float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
		do{
			if (itemstack.stackSize <= 0){
				break;
			}
			int i1 = world.rand.nextInt(21) + 10;
			if (i1 > itemstack.stackSize){
				i1 = itemstack.stackSize;
			}
			itemstack.stackSize -= i1;
			ItemStack newItem = new ItemStack(itemstack.getItem(), i1, itemstack.getItemDamage());
			newItem.setTagCompound(itemstack.getTagCompound());
			EntityItem entityitem = new EntityItem(world, i + f, j + f1, k + f2, newItem);
			float f3 = 0.05F;
			entityitem.motionX = (float)world.rand.nextGaussian() * f3;
			entityitem.motionY = (float)world.rand.nextGaussian() * f3 + 0.2F;
			entityitem.motionZ = (float)world.rand.nextGaussian() * f3;
			world.spawnEntityInWorld(entityitem);
		}while (true);

		world.setBlockToAir(pos.up());

		super.breakBlock(world, i, j, k, par5, metadata);
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z){
		if (world.isRemote)
			return false;

		if (world.getBlock(x, y - 1, z) == BlocksCommonProxy.keystoneDoor)
			y--;

		IKeystoneLockable lockable = (IKeystoneLockable)world.getTileEntity(x, y, z);

		if (lockable == null)
			return false;

		if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK)) return false;

		return super.removedByPlayer(world, player, x, y, z);
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player){
		if (world.isRemote)
			return;

		if (world.getBlock(x, y - 1, z) == BlocksCommonProxy.keystoneDoor)
			y--;

		IKeystoneLockable lockable = (IKeystoneLockable)world.getTileEntity(x, y, z);

		if (lockable == null)
			return;

		if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK))
			return;
		super.onBlockHarvested(world, x, y, z, meta, player);
	}

	@Override
	public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_){
		return ItemsCommonProxy.itemKeystoneDoor;
	}

	@Override
	public int getDamageValue(World p_149643_1_, int p_149643_2_, int p_149643_3_, int p_149643_4_){
		return ItemsCommonProxy.itemKeystoneDoor.SPELL_SEALED_DOOR;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i){
		return new TileEntitySpellSealedDoor();
	}

	@Override
	public int getRenderBlockPass(){
		return 1;
	}

	public boolean applyComponentToDoor(IBlockAccess access, ISpellComponent component, int x, int y, int z){

		if (access.getBlock(x, y - 1, z) == BlocksCommonProxy.spellSealedDoor)
			y--;


		TileEntity te = access.getTileEntity(x, y, z);
		if (te == null || te instanceof TileEntitySpellSealedDoor == false){
			return false;
		}

		((TileEntitySpellSealedDoor)te).addPartToCurrentKey(component);

		return true;
	}

	public void setDoorState(World world, int x, int y, int z, EntityPlayer player, boolean open){
		int i1 = this.func_150012_g(world, x, y, z);
		int j1 = i1 & 7;
		j1 ^= 4;

		if ((i1 & 8) == 0){
			world.setBlockMetadataWithNotify(x, y, z, j1, 2);
			world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
		}else{
			world.setBlockMetadataWithNotify(x, y - 1, z, j1, 2);
			world.markBlockRangeForRenderUpdate(x, y - 1, z, x, y, z);
		}

		world.playAuxSFXAtEntity(player, 1003, x, y, z, 0);
	}
}
