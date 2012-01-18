package au.gov.ga.worldwind.animator.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper that allows connection between multiple {@link Armable}s or multiple
 * {@link Enableable}s. This ensures that, when one of the objects is armed or
 * enabled, all the other 'co-dependant' objects are also armed or enabled. This
 * is useful for objects for which it doesn't make sense to enable/arm them
 * individually (such as individual camera parameters).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CodependantHelper
{
	private final Armable armableOwner;
	private final Enableable enableableOwner;
	private final Set<Armable> codependantArmables = new HashSet<Armable>();
	private final Set<Enableable> codependantEnableable = new HashSet<Enableable>();

	public CodependantHelper(Armable armableOwner, Enableable enableableOwner)
	{
		this.armableOwner = armableOwner;
		this.enableableOwner = enableableOwner;
	}

	public void addCodependantArmable(Armable armable)
	{
		if (armableOwner != null && !codependantArmables.contains(armable))
		{
			codependantArmables.add(armable);
			armable.connectCodependantArmable(armableOwner);
		}
	}

	public void setCodependantArmed(boolean armed)
	{
		for (Armable armable : codependantArmables)
		{
			if (armable.isArmed() != armed)
			{
				armable.setArmed(armed);
			}
		}
	}

	public void addCodependantEnableable(Enableable enableable)
	{
		if (enableableOwner != null && !codependantEnableable.contains(enableable))
		{
			codependantEnableable.add(enableable);
			enableable.connectCodependantEnableable(enableableOwner);
		}
	}

	public void setCodependantEnabled(boolean enabled)
	{
		for (Enableable enableable : codependantEnableable)
		{
			if (enableable.isEnabled() != enabled)
			{
				enableable.setEnabled(enabled);
			}
		}
	}
}
