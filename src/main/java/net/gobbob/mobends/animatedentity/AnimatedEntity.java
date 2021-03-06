package net.gobbob.mobends.animatedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.animatedentity.previewer.PlayerPreviewer;
import net.gobbob.mobends.animatedentity.previewer.Previewer;
import net.gobbob.mobends.animatedentity.previewer.SpiderPreviewer;
import net.gobbob.mobends.client.mutators.PigZombieMutator;
import net.gobbob.mobends.client.mutators.PlayerMutator;
import net.gobbob.mobends.client.mutators.SpiderMutator;
import net.gobbob.mobends.client.mutators.SquidMutator;
import net.gobbob.mobends.client.mutators.ZombieMutator;
import net.gobbob.mobends.client.mutators.functions.ApplyMutationFunction;
import net.gobbob.mobends.client.mutators.functions.DeapplyMutationFunction;
import net.gobbob.mobends.client.renderer.entity.MutatedRenderer;
import net.gobbob.mobends.client.renderer.entity.PlayerRenderer;
import net.gobbob.mobends.client.renderer.entity.RenderBendsSpectralArrow;
import net.gobbob.mobends.client.renderer.entity.RenderBendsTippedArrow;
import net.gobbob.mobends.client.renderer.entity.SpiderRenderer;
import net.gobbob.mobends.client.renderer.entity.SquidRenderer;
import net.gobbob.mobends.client.renderer.entity.ZombieRenderer;
import net.gobbob.mobends.util.BendsLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class AnimatedEntity
{
	public static HashMap<String, AnimatedEntity> animatedEntities = new HashMap<String, AnimatedEntity>();

	private String name;
	private String displayName;
	private List<AlterEntry> alterEntries = new ArrayList<AlterEntry>();
	private String[] alterableParts;
	private MutatedRenderer renderer;

	public Class<? extends Entity> entityClass;
	private final ApplyMutationFunction applyMutation;
	private final DeapplyMutationFunction deapplyMutation;
	private final Runnable refreshMutation;

	public Previewer previewer;

	public AnimatedEntity(String id, String displayName, Class<? extends Entity> entityClass,
			ApplyMutationFunction applyMutation, DeapplyMutationFunction deapplyMutation, Runnable refreshMutation,
			MutatedRenderer renderer, String[] alterableParts)
	{
		this.name = id;
		this.displayName = displayName;
		this.entityClass = entityClass;
		this.applyMutation = applyMutation;
		this.deapplyMutation = deapplyMutation;
		this.refreshMutation = refreshMutation;
		this.renderer = renderer;
		this.alterableParts = alterableParts;
		this.addAlterEntry(new AlterEntry(this, displayName));
	}

	public List<AlterEntry> getAlredEntries()
	{
		return this.alterEntries;
	}

	public AlterEntry getAlterEntry(int index)
	{
		return this.alterEntries.get(index);
	}

	public String[] getAlterableParts()
	{
		return alterableParts;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getName()
	{
		return name;
	}

	public Previewer getPreviewer()
	{
		return this.previewer;
	}

	/*
	 * Returns true if any of the alter entries are animated
	 */
	public boolean isAnimated()
	{
		for (AlterEntry entry : this.alterEntries)
		{
			if (entry.isAnimated())
				return true;
		}
		return false;
	}

	public AnimatedEntity addAlterEntry(AlterEntry alterEntry)
	{
		this.alterEntries.add(alterEntry);
		return this;
	}

	public AnimatedEntity setPreviewer(Previewer previewer)
	{
		this.previewer = previewer;
		return this;
	}

	public void beforeRender(EntityLivingBase entity, float partialTicks)
	{
		if (this.renderer != null)
			this.renderer.beforeRender(entity, partialTicks);
	}

	public void afterRender(EntityLivingBase entity, float partialTicks)
	{
		if (this.renderer != null)
			this.renderer.afterRender(entity, partialTicks);
	}

	public void applyMutation(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity,
			float partialTicks)
	{
		applyMutation.apply(renderer, entity, partialTicks);
	}

	public void deapplyMutation(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity)
	{
		deapplyMutation.deapply(renderer, entity);
	}

	public void refreshMutation()
	{
		refreshMutation.run();
	}

	public static void register(Configuration config)
	{
		BendsLogger.info("Registering Animated Entities...");

		animatedEntities.clear();

		registerEntity(config,
				new AnimatedEntity("player", "Player", AbstractClientPlayer.class, PlayerMutator::apply,
						PlayerMutator::deapply, PlayerMutator::refresh, new PlayerRenderer(),
						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
								"rightLeg", "leftForeLeg", "rightForeLeg", "totalRotation", "leftItemRotation",
								"rightItemRotation" }).setPreviewer(new PlayerPreviewer()));

		registerEntity(config,
				new AnimatedEntity("pig_zombie", "Zombie Pigman", EntityPigZombie.class, PigZombieMutator::apply,
						PigZombieMutator::deapply, PigZombieMutator::refresh, new ZombieRenderer(),
						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
								"rightLeg", "leftForeLeg", "rightForeLeg" }));

		registerEntity(config,
				new AnimatedEntity("zombie", "Zombie", EntityZombie.class, ZombieMutator::apply, ZombieMutator::deapply,
						ZombieMutator::refresh, new ZombieRenderer(),
						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
								"rightLeg", "leftForeLeg", "rightForeLeg" }));

		registerEntity(config,
				new AnimatedEntity("spider", "Spider", EntitySpider.class, SpiderMutator::apply, SpiderMutator::deapply,
						SpiderMutator::refresh, new SpiderRenderer(),
						new String[] { "head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7",
								"leg8", "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5", "foreLeg6",
								"foreLeg7", "foreLeg8" }).setPreviewer(new SpiderPreviewer()));
		
		registerEntity(config,
				new AnimatedEntity("squid", "Squid", EntitySquid.class, SquidMutator::apply, SquidMutator::deapply,
						SquidMutator::refresh, new SquidRenderer(),
						new String[] { "body", "tentacle1", "tentacle2", "tentacle3", "tentacle4", "tentacle5", "tentacle6", "tentacle7", "tentacle8" }));

		/*
		 * registerEntity(config, new AnimatedEntity("husk", "Husk", EntityHusk.class,
		 * new RenderBendsHusk(Minecraft.getMinecraft().getRenderManager()), new
		 * String[] {"head", "body", "leftArm", "rightArm", "leftForeArm",
		 * "rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}));
		 * registerEntity(config, new AnimatedEntity("spider", "Spider",
		 * EntitySpider.class, new
		 * RenderBendsSpider(Minecraft.getMinecraft().getRenderManager()), new String[]
		 * {"head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6",
		 * "leg7", "leg8", "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5",
		 * "foreLeg6", "foreLeg7", "foreLeg8"})); registerEntity(config, new
		 * AnimatedEntity("cave_spider", "Cave Spider", EntityCaveSpider.class, new
		 * RenderBendsCaveSpider(Minecraft.getMinecraft().getRenderManager()), new
		 * String[] {"head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5",
		 * "leg6", "leg7", "leg8", "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4",
		 * "foreLeg5", "foreLeg6", "foreLeg7", "foreLeg8"})); registerEntity(config, new
		 * AnimatedEntity("skeleton", "Skeleton", EntitySkeleton.class, new
		 * RenderBendsSkeleton(Minecraft.getMinecraft().getRenderManager()), new
		 * String[] {"head", "body", "leftArm", "rightArm", "leftForeArm",
		 * "rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}));
		 * registerEntity(config, new AnimatedEntity("wither_skeleton",
		 * "Wither Skeleton", EntityWitherSkeleton.class, new
		 * RenderBendsWitherSkeleton(Minecraft.getMinecraft().getRenderManager()), new
		 * String[] {"head", "body", "leftArm", "rightArm", "leftForeArm",
		 * "rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}));
		 * registerEntity(config, new AnimatedEntity("stray", "Stray",
		 * EntityStray.class, new
		 * RenderBendsStray(Minecraft.getMinecraft().getRenderManager()), new String[]
		 * {"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
		 * "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}));
		 */

		RenderingRegistry.registerEntityRenderingHandler(EntitySpectralArrow.class, new RenderBendsSpectralArrow.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityTippedArrow.class, new RenderBendsTippedArrow.Factory());
	}

	public static void registerEntity(Configuration config, AnimatedEntity animatedEntity)
	{
		BendsLogger.info("Registering " + animatedEntity.displayName);
		for (AlterEntry alterEntry : animatedEntity.alterEntries)
		{
			alterEntry.setAnimate(config.get("Animated", alterEntry.getName(), true).getBoolean());
		}
		animatedEntities.put(animatedEntity.name, animatedEntity);
	}

	public static AnimatedEntity get(String name)
	{
		return animatedEntities.get(name);
	}

	public static AnimatedEntity getForEntity(Entity entity)
	{
		// Checking direct registration
		Class<? extends Entity> entityClass = entity.getClass();
		for (AnimatedEntity animatedEntity : animatedEntities.values())
			if (animatedEntity.entityClass.equals(entityClass))
				return animatedEntity;

		// Checking indirect inheritance
		for (AnimatedEntity animatedEntity : animatedEntities.values())
			if (animatedEntity.entityClass.isInstance(entity))
				return animatedEntity;

		return null;
	}

	public static void refreshMutators()
	{
		for (AnimatedEntity animatedEntity : animatedEntities.values())
			animatedEntity.refreshMutation();
	}
}
