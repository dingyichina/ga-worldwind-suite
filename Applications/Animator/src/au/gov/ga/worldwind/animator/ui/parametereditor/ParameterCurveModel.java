package au.gov.ga.worldwind.animator.ui.parametereditor;


/**
 * An interface for a model that backs the {@link ParameterCurve} component
 */
public interface ParameterCurveModel
{

	/** Destroys this curve. Once called, no further updates will take place for the curve. */
	void destroy();
	
	/**
	 * Acquire a lock on the model, allowing multiple reads from the model.
	 * <p/>
	 * Prevents the model from being updated until the {@link #unlock()} method is called.
	 */
	void lock();
	
	/**
	 * Unlock the model, allowing the model to be updated.
	 */
	void unlock();

	/**
	 * @return The parameter value at the provided frame
	 */
	double getValueAtFrame(int frame);

	/**
	 * @return The minimum value of the parameter this model is reflecting
	 */
	double getMinValue();
	
	/**
	 * @return The maximum value of the parameter this model is reflecting
	 */
	double getMaxValue();

	/**
	 * @return The lowest frame of the parameter this model is reflecting
	 */
	int getMinFrame();
	
	/**
	 * @return The highest frame of the parameter this model is reflecting
	 */
	int getMaxFrame();
}
