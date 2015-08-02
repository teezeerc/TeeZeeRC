/*
 *  GNU GENERAL PUBLIC LICENSE Version 2, June 1991
 */
package pl.zumbrzycki.teezeerc.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author Tomasz Zumbrzycki Tests for utils
 * 
 */
public class UtilsTest {

	@Test
	public void shouldComputeFletcherForZeros() {
		// given
		byte[] byteArray = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		// when
		byte[] result = Utils.fletcher16(byteArray);
		// then
		assertThat(result).isEqualTo(new byte[] { 0, 0 });
	}

	@Test
	public void shouldComputeFletcherFor255() {
		// given
		byte[] byteArray = new byte[] { (byte) 255, (byte) 255, (byte) 255,
				(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
		// when
		byte[] result = Utils.fletcher16(byteArray);
		// then
		assertThat(result).isEqualTo(new byte[] { 0, 0 });
	}

	@Test
	public void shouldComputeFletcherFor127() {
		// given
		byte[] byteArray = new byte[] { (byte) 127, (byte) 127, (byte) 127,
				(byte) 127, (byte) 127, (byte) 127, (byte) 127, (byte) 127 };
		// when
		byte[] result = Utils.fletcher16(byteArray);
		// then
		assertThat(result).isEqualTo(new byte[] { (byte) 237, (byte) 251 });
	}

	@Test
	public void shouldComputeFletcherFor1AndZeroes() {
		// given
		byte[] byteArray = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0 };
		// when
		byte[] result = Utils.fletcher16(byteArray);
		// then
		assertThat(result).isEqualTo(new byte[] { 9, 1 });
	}

	@Test
	public void shouldConstructStandardPacketForZerosAndFletcher() {
		// given
		Map<Integer, Integer> channels = new HashMap<>();
		channels.put(1, 0);
		channels.put(2, 0);
		channels.put(3, 0);
		channels.put(4, 0);
		// when
		byte[] result = Utils.constructPacket(channels);
		// then
		assertThat(result).isEqualTo(
				new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 9, 1 });
	}

	@Test
	public void shouldMap5To10() {
		// given
		float value = 5;
		// when
		float result = Utils.map(value, 0, 10, 5, 15);
		// then
		assertThat(result).isEqualTo(10);
	}

	@Test
	public void shouldMap508To127() {
		// given
		float value = 508;
		// when
		float result = Utils.map(value, 1, 1016, 0, 254);
		// then
		assertThat(result).isEqualTo(127);
	}

}
