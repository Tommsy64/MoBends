package net.gobbob.mobends.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class EntityDatabase
{
	public static EntityDatabase instance = new EntityDatabase();

	// Contains data for all EntityData instances.
	protected HashMap<Integer, EntityData> entryMap;

	public EntityDatabase()
	{
		this.entryMap = new HashMap<Integer, EntityData>();
	}

	public EntityData get(Integer identifier)
	{
		return entryMap.get(identifier);
	}

	public EntityData get(Entity entity)
	{
		return this.get(entity.getEntityId());
	}

	/*
	 * If a data instance for that identifier is null, create one. Return the data
	 * instance for that identifier.
	 */
	public <T extends EntityData, E extends Entity> T getAndMake(Function<E, T> dataCreationFunction, E entity)
	{
		final int entityId = entity.getEntityId();
		
		T data = null;
		try
		{
			// We try to get and cast the data to the appropriate type.
			data = (T) this.get(entityId);
		}
		catch (ClassCastException e)
		{
			// The data that is located inside the database is of the wrong type,
			// so we remove the old one, and add a new one.
			this.remove(entityId);
			data = null;
		}
		
		if (data == null)
		{
			data = dataCreationFunction.apply(entity);
			if (data != null)
			{
				this.add(entityId, data);
			}
		}
		return data;
	}

	private void add(int identifier, EntityData data)
	{
		this.entryMap.put(identifier, data);
	}

	public void add(Entity entity, EntityData data)
	{
		this.add(entity.getEntityId(), data);
	}

	private void remove(int identifier)
	{
		this.entryMap.remove(identifier);
	}

	public void updateClient()
	{
		Iterator<Integer> it = entryMap.keySet().iterator();
		while (it.hasNext())
		{
			Integer key = it.next();
			EntityData entityData = get(key);
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(key);
			if (entity != null)
			{
				if (entityData.getEntity() != entity)
				{
					System.out.println("Removing");
					it.remove();
				} else
				{
					entityData.updateClient(entity);
				}
			} else
			{
				it.remove();
				this.remove(key);
			}
		}
	}

	public void updateRender(float partialTicks)
	{
		for (EntityData entityData : this.entryMap.values())
		{
			if (entityData.canBeUpdated())
				entityData.update(partialTicks);
		}
	}

	public void refresh()
	{
		this.entryMap.clear();
	}
}
