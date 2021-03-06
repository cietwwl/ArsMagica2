package am2.affinity;

import am2.ArsMagica2;
import am2.affinity.abilities.*;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.event.SpellCastEvent;
import am2.extensions.AffinityData;
import am2.utils.WorldUtils;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.Map.Entry;

public class AffinityAbilityHelper {
	
	static {
		//AIR
		GameRegistry.register(new AbilityLightAsAFeather());
		GameRegistry.register(new AbilityAgile());
		
		//ARCANE
		GameRegistry.register(new AbilityClearCaster());
		GameRegistry.register(new AbilityMagicWeakness());
		GameRegistry.register(new AbilityOneWithMagic());
		
		//EARTH
		GameRegistry.register(new AbilitySolidBones());
		
		//ENDER
		GameRegistry.register(new AbilityRelocation());
		GameRegistry.register(new AbilityNightVision());
		GameRegistry.register(new AbilityWaterWeakness(Affinity.ENDER));
		GameRegistry.register(new AbilityPoisonResistance());
		GameRegistry.register(new AbilitySunlightWeakness());
		
		//FIRE
		GameRegistry.register(new AbilityFireResistance());
		GameRegistry.register(new AbilityFirePunch());
		GameRegistry.register(new AbilityWaterWeakness(Affinity.FIRE));
		
		//ICE
		GameRegistry.register(new AbilityLavaFreeze());
		GameRegistry.register(new AbilityWaterFreeze());
		GameRegistry.register(new AbilityColdBlooded());
		
		//LIFE
		GameRegistry.register(new AbilityFastHealing());
		GameRegistry.register(new AbilityPacifist());
		
		//WATER
		GameRegistry.register(new AbilityExpandedLungs());
		GameRegistry.register(new AbilityFluidity());
		GameRegistry.register(new AbilitySwiftSwim());
		GameRegistry.register(new AbilityFireWeakness());
		GameRegistry.register(new AbilityAntiEndermen());
		
		//NATURE
		GameRegistry.register(new AbilityRooted());
		GameRegistry.register(new AbilityThorns());
		GameRegistry.register(new AbilityLeafLike());
		GameRegistry.register(new AbilityPhotosynthesis());
		
		//LIGHTNING
		GameRegistry.register(new AbilityLightningStep());
		GameRegistry.register(new AbilityReflexes());
		GameRegistry.register(new AbilityFulmination());
		GameRegistry.register(new AbilityShortCircuit());
		GameRegistry.register(new AbilityThunderPunch());
		GameRegistry.register(new AbilityWaterWeakness(Affinity.LIGHTNING));
	}
	
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
			if (ability.getKey() != null && ability.getKey().isPressed()) {
				EntityPlayer player = ArsMagica2.proxy.getLocalPlayer();
				//if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
				//	return;

				player = player.getEntityWorld().getPlayerEntityByUUID(player.getUniqueID());
				if (ability.canApply(player)) {

					WorldUtils.runSided(Side.CLIENT, ability.createRunnable(ArsMagica2.proxy.getLocalPlayer()));
//					WorldUtils.runSided(Side.SERVER, ability.createRunnable(player));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerTick(LivingUpdateEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
//			if (!event.getEntityLiving().world.isRemote) {
				Map<String, Integer> m = AffinityData.For(event.getEntityLiving()).getCooldowns();
				for (Entry<String, Integer> entry : m.entrySet()) {
					if (entry.getValue() > 0)
						AffinityData.For(event.getEntityLiving()).addCooldown(entry.getKey(), entry.getValue() - 1);
				}
//			}
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyTick((EntityPlayer) event.getEntityLiving());
				else
					ability.removeEffects((EntityPlayer) event.getEntityLiving());
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerHurt(LivingHurtEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyHurt((EntityPlayer) event.getEntityLiving(), event, false);
			}
		}
		if (event.getSource().getEntity() != null && event.getSource().getEntity() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getSource().getEntity()))
					ability.applyHurt((EntityPlayer) event.getSource().getEntity(), event, true);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerFall(LivingFallEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyFall((EntityPlayer) event.getEntityLiving(), event);
			}
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyDeath((EntityPlayer) event.getEntityLiving(), event);
			}
		}
		if (event.getSource().getEntity() != null && event.getSource().getEntity() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getSource().getEntity()))
					ability.applyKill((EntityPlayer) event.getSource().getEntity(), event);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerJump(LivingJumpEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyJump((EntityPlayer) event.getEntityLiving(), event);
			}
		}
	}
	
	@SubscribeEvent
	public void onSpellCast(SpellCastEvent.Post event) {
		if (event.entityLiving instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.entityLiving))
					ability.applySpellCast((EntityPlayer) event.entityLiving, event);
			}
		}
	}
	
	@SubscribeEvent
	public void onPreSpellCast(SpellCastEvent.Pre event) {
		if (event.entityLiving instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.entityLiving))
					ability.applyPreSpellCast((EntityPlayer) event.entityLiving, event);
			}
		}
	}

	public static boolean isBareHanded(EntityPlayer player) {
		ItemStack is = player.getHeldItemMainhand();
		return is == null || is.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
				.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).stream()
				.mapToDouble(AttributeModifier::getAmount).max().orElse(-1.0D) <= 0.0D;
	}
}
