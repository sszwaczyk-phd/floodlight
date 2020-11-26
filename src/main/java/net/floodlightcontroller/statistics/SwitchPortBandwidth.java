package net.floodlightcontroller.statistics;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.statistics.web.SwitchPortBandwidthSerializer;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@JsonSerialize(using=SwitchPortBandwidthSerializer.class)
public class SwitchPortBandwidth {

	private Logger log = LoggerFactory.getLogger(SwitchPortBandwidth.class);

	private DatapathId id;
	private OFPort pt;
	private U64 speed; //kb/s
	private U64 rx;
	private double rxUtilization;
	private double rxUtilizationPercent;
	private double availableRxBandwidth; //kb/s
	private U64 tx;
	private double txUtilization;
	private double txUtilizationPercent;
	private double availableTxBandwidth; //kb/s
	private Date time;
	private long starttime_ns;
	private U64 rxValue;
	private U64 txValue;
	
	private SwitchPortBandwidth() {}
	private SwitchPortBandwidth(DatapathId d, OFPort p, U64 s, U64 rx, U64 tx, U64 rxValue, U64 txValue) {
		id = d;
		pt = p;
		speed = s;
//		//Set speed to 10 Mb/s (100 000 kb/s) becouse OVS reads 10 GB/s from NIC
//		speed = U64.of(10000);
		log.debug("Speed of switch (" + d + ") of port (" + p + ") is " + speed.getValue() + "kb/s");
		this.rx = rx;
		this.tx = tx;
		time = new Date();
		starttime_ns = System.nanoTime();
		this.rxValue = rxValue;
		this.txValue = txValue;
		if(speed.getValue() != 0) {
			this.rxUtilization = ((double) (rx.getValue() / 1000)) / (double) speed.getValue();
			log.debug("Rx utilization is " + rxUtilization);
			this.txUtilization = ((double) (tx.getValue() / 1000)) / (double) speed.getValue();
			log.debug("Tx utilization is " + txUtilization);

			this.rxUtilizationPercent = rxUtilization * 100;
			log.debug("Rx utilization percent is " + rxUtilizationPercent + " %");
			this.txUtilizationPercent = txUtilization * 100;
			log.debug("Tx utilization percent is " + txUtilizationPercent + " %");

			this.availableRxBandwidth = speed.getValue() - (speed.getValue() * rxUtilization);
			log.debug("Available rx bandwidth is " + availableRxBandwidth);
			this.availableTxBandwidth = speed.getValue() - (speed.getValue() * txUtilization);
			log.debug("Available tx bandwidth is " + availableTxBandwidth);
		}
	}
	
	public static SwitchPortBandwidth of(DatapathId d, OFPort p, U64 s, U64 rx, U64 tx, U64 rxValue, U64 txValue) {
		if (d == null) {
			throw new IllegalArgumentException("Datapath ID cannot be null");
		}
		if (p == null) {
			throw new IllegalArgumentException("Port cannot be null");
		}
		if (s == null) {
			throw new IllegalArgumentException("Link speed cannot be null");
		}
		if (rx == null) {
			throw new IllegalArgumentException("RX bandwidth cannot be null");
		}
		if (tx == null) {
			throw new IllegalArgumentException("TX bandwidth cannot be null");
		}
		if (rxValue == null) {
			throw new IllegalArgumentException("RX value cannot be null");
		}
		if (txValue == null) {
			throw new IllegalArgumentException("TX value cannot be null");
		}
		return new SwitchPortBandwidth(d, p, s, rx, tx, rxValue, txValue);
	}
	
	public DatapathId getSwitchId() {
		return id;
	}
	
	public OFPort getSwitchPort() {
		return pt;
	}
	
	public U64 getLinkSpeedBitsPerSec() {
		return speed;
	}
	
	public U64 getBitsPerSecondRx() {
		return rx;
	}
	
	public U64 getBitsPerSecondTx() {
		return tx;
	}
	
	public U64 getPriorByteValueRx() {
		return rxValue;
	}
	
	public U64 getPriorByteValueTx() {
		return txValue;
	}
	
	public long getUpdateTime() {
		return time.getTime();
	}

	public long getStartTime_ns() {
		return starttime_ns;
	}

	public double getRxUtilization() {
		return rxUtilization;
	}

	public double getTxUtilization() {
		return txUtilization;
	}

	public double getRxUtilizationPercent() {
		return rxUtilizationPercent;
	}

	public double getTxUtilizationPercent() {
		return txUtilizationPercent;
	}
	
	public double getAvailableRxBandwidth() {
		return availableRxBandwidth;
	}

	public double getAvailableTxBandwidth() {
		return availableTxBandwidth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((pt == null) ? 0 : pt.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwitchPortBandwidth other = (SwitchPortBandwidth) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (pt == null) {
			if (other.pt != null)
				return false;
		} else if (!pt.equals(other.pt))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SwitchPortBandwidth{" +
				"id=" + id +
				", pt=" + pt +
				", speed=" + speed.getValue() +
				", rxUtilization=" + rxUtilization +
				", txUtilization=" + txUtilization +
				'}';
	}
}