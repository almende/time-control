/* $Id$
 * $URL$
 * 
 * Part of the EU project Inertia, see http://www.inertia-project.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2014 Almende B.V. 
 */
package com.almende.timecontrol.time;

import io.coala.error.ExceptionBuilder;
import io.coala.json.Wrapper;

import javax.measure.DecimalMeasure;
import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.joda.time.Period;
import org.joda.time.ReadableDuration;
import org.jscience.physics.amount.Amount;
import org.threeten.bp.temporal.TemporalAmount;

import com.almende.timecontrol.TimeControl;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain Duration} wraps an {@linkplain TimeSpan} that is
 * {@linkplain Polymorphic} and provides a {@link #valueOf(String)} method
 * for loading as configured value {@link Converters#CLASS_WITH_VALUE_OF_METHOD}
 * <p>
 * We considered various temporal measure implementations, including
 * <dl>
 * <dt>Java's native utilities</dt>
 * <dd>Offers no standard tuple combining a {@link java.lang.Number} and
 * {@link java.util.concurrent.TimeUnit}</dd>
 * <dt>The JSR-275 {@code javax.measure} reference implementation v4.3.1 from <a
 * href="http://jscience.org/">jscience.org</a></dt>
 * <dd>
 * <li>takes any value type (e.g. {@linkplain Number}) or granularity (e.g.
 * {@link SI#NANO(javax.measure.unit.Unit nano)} or
 * {@link SI#PICO(javax.measure.unit.Unit) pico})</li></dd>
 * <dt>The JSR-310 {@code javax.time} Java8 extension back-port from <a
 * href="http://www.threeten.org/">threeten.org</a>:</dt>
 * <dd>
 * <li>supports nanosecond precision,</li>
 * <li>{@linkplain org.threeten.bp.Duration} parses strictly 'PTx.xS' (upper
 * case) ISO8601 format only</li>
 * <li>{@linkplain org.threeten.bp.temporal.TemporalAmount} does not align with
 * earlier JSR-275 {@link javax.measure.quantity.Duration}</li></dd>
 * <dt>Joda's time API from <a href="http://www.joda.org/">joda.org</a></dt>
 * <dd>
 * <li>Allows lenient (lower and upper case) ISO8601 format strings</li>
 * <li>{@link org.joda.time.Duration} implements {@link Comparable} whereas
 * {@link org.joda.time.Period} does not.</li>
 * <li>Joda time offers this <a
 * href="https://github.com/FasterXML/jackson-datatype-joda">datatype extension
 * for Jackson</a>.</li>
 * <li>offers many nice calendar and formatter implementations</li>
 * <li>will <a href="https://github.com/JodaOrg/joda-time/issues/52">not support
 * microsecond or nanosecond precision</a></li></dd>
 * <dt>Apache {@code commons-lang3} Date Utilities</dt>
 * <dd>
 * limitations similar to Joda's time API (millisecond precision only)</dd>
 * <dt>Guava in the Google Web Toolkit from <a
 * href="https://github.com/google/guava">github.com/google/guava</a></dt>
 * <dd>
 * extends relevant Java types only with a (time-line offset) interval (
 * {@code Range}) API, not a (free floating) duration quantity</dd>
 * <dt>DESMO-J's TimeSpan API from <a
 * href="http://desmoj.sf.net/">desmoj.sf.net</a></dt>
 * <dd>limited to Java's standard TimeUnit</dd>
 * <dt>DSOL3's UnitTime API from <a
 * href="http://simulation.tudelft.nl/">simulation.tudelft.nl</a></dt>
 * <dd>no Javadoc available</dd>
 * </dl>
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class Duration implements Wrapper<TimeSpan>, Comparable<Duration>
{

	/** */
	public static final Duration ZERO = Duration.valueOf("0 ms");

	/** */
	private TimeSpan value;

	@Override
	public TimeSpan getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(final TimeSpan value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return getValue().toString();
	}

	@Override
	public int hashCode()
	{
		return getValue().hashCode();
	}

	@Override
	public int compareTo(final Duration that)
	{
		return getValue().compareTo(that.getValue());
	}

	/**
	 * @param unit
	 * @return
	 */
	public Duration to(final Unit<javax.measure.quantity.Duration> unit)
	{
		return valueOf(getValue().to(unit));
	}

	@JsonIgnore
	public long longValue(final Unit<javax.measure.quantity.Duration> unit)
	{
		try
		{
			return getValue().longValue(unit);
		} catch (final Throwable t)
		{
			throw ExceptionBuilder.unchecked(
					"Could not convert " + getValue().getUnit().getClass()
							+ " to " + unit, t).build();
		}
	}

	@JsonIgnore
	public long toMillisLong()
	{
		return longValue(TimeControl.MILLIS);
	}

	@JsonIgnore
	public long toNanosLong()
	{
		return longValue(TimeControl.NANOS);
	}

	/** @return the Joda {@link ReadableDuration} implementation of a time span */
	@JsonIgnore
	public ReadableDuration toJoda()
	{
		return org.joda.time.Duration.millis(toMillisLong());
	}

	/**
	 * @return a JSR-310 {@link TemporalAmount} implementation of a time span,
	 *         either time-based ({@link org.threeten.bp.Duration}) or
	 *         date-based ({@link org.threeten.bp.Period})
	 */
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public <T extends TemporalAmount & Comparable<T>> T toJava8()
	{
		return (T) org.threeten.bp.Duration.ofNanos(toNanosLong());
	}

	/** @return the JScience {@link Amount} implementation of a time span */
	@JsonIgnore
	public Amount<javax.measure.quantity.Duration> toAmount()
	{
		return Amount.valueOf(getValue().toString()).to(getValue().getUnit());
	}

	/** @return the JSR-275 {@link Measurable} implementation of a time span */
	@JsonIgnore
	public Measurable<javax.measure.quantity.Duration> toMeasure()
	{
		return getValue();
	}

	/**
	 * for "natural" Config value conversion for a {@link Duration} (i.e.
	 * {@link TimeSpan}).
	 * 
	 * @param value a duration as {@link DecimalMeasure JSR-275} measure (e.g.
	 *            {@code "123 ms"}) or as ISO Period, parsed with
	 *            {@link org.threeten.bp.Duration#parse(CharSequence) JSR-310}
	 *            or {@link Period#parse(String) Joda}.
	 * 
	 *            Examples of ISO period:
	 * 
	 *            <pre>
	 *    "PT20.345S" -> parses as "20.345 seconds"
	 *    "PT15M"     -> parses as "15 minutes" (where a minute is 60 seconds)
	 *    "PT10H"     -> parses as "10 hours" (where an hour is 3600 seconds)
	 *    "P2D"       -> parses as "2 days" (where a day is 24 hours or 86400 seconds)
	 *    "P2DT3H4M"  -> parses as "2 days, 3 hours and 4 minutes"
	 *    "P-6H3M"    -> parses as "-6 hours and +3 minutes"
	 *    "-P6H3M"    -> parses as "-6 hours and -3 minutes"
	 *    "-P-6H+3M"  -> parses as "+6 hours and -3 minutes"
	 * </pre>
	 * 
	 * @see org.aeonbits.owner.Converters.CLASS_WITH_VALUE_OF_METHOD
	 * @see org.threeten.bp.Duration#parse(String)
	 * @see org.joda.time.format.ISOPeriodFormat#standard()
	 * @see DecimalMeasure
	 */
	public static Duration valueOf(final String value)
	{
		return valueOf(TimeSpan.valueOf(value));
	}

	/**
	 * {@link Duration} static factory method
	 * 
	 * @param value
	 */
	public static Duration valueOf(final ReadableDuration value)
	{
		return valueOf(TimeSpan.valueOf(value));
	}

	/**
	 * {@link Duration} static factory method
	 * 
	 * @param value
	 */
	public static Duration valueOf(
			final Measure<? extends Number, javax.measure.quantity.Duration> value)
	{
		return valueOf(TimeSpan.valueOf(value));
	}

	/**
	 * {@link Duration} static factory method
	 * 
	 * @param value
	 */
	public static Duration valueOf(
			final Amount<javax.measure.quantity.Duration> value)
	{
		return valueOf(TimeSpan.valueOf(value));
	}

	/**
	 * {@link Duration} static factory method
	 * 
	 * @param value the number of milliseconds
	 */
	public static Duration valueOf(final Number value)
	{
		return valueOf(TimeSpan.valueOf(value));
	}

	/**
	 * {@link Duration} static factory method
	 * 
	 * @param value the {@link TimeSpan}
	 */
	public static Duration valueOf(final TimeSpan value)
	{
		return new Duration()
		{
			{
				setValue(value);
			}
		};
	}
}