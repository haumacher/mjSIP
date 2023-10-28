/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media;

import org.mjsip.media.rx.RtpReceiverOptions;
import org.mjsip.media.tx.RtpSenderOptions;
import org.mjsip.rtp.RtpControl;
import org.mjsip.sound.Codec;

/**
 * Options for {@link AudioStreamer}.
 */
public interface StreamerOptions extends RtpSenderOptions, RtpReceiverOptions {

	/**
	 * Whether to use {@link RtpControl}.
	 */
	boolean rtp();

	/**
	 * Optional additional audio encoder/decoder.
	 */
	Codec additionalCodec();

	/**
	 * Whether to use symmetric rtp.
	 */
	boolean symmetricRtp();

	/**
	 * Creates an options builder.
	 */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for {@link StreamerOptions}.
	 */
	public class Builder {
		private boolean _rtp;
	
		private Codec _codec;
	
		private boolean _symmetricRtp;

		private boolean _sequenceCheck;

		private boolean _silencePadding;

		private int _red;

		private long _syncAdjust;

		private boolean _ssrcCheck;

		/**
		 * Creates a {@link StreamerOptions.Builder}.
		 * 
		 * @see StreamerOptions#builder()
		 */
		private Builder() {
			super();
		}
	
		/**
		 * @see StreamerOptions#rtp()
		 */
		public Builder setRtp(boolean value) {
			_rtp = value;
			return this;
		}
	
		/**
		 * @see StreamerOptions#additionalCodec()
		 */
		public Builder setAdditionalCodec(Codec codec) {
			_codec = codec;
			return this;
		}
	
		/**
		 * @see StreamerOptions#symmetricRtp()
		 */
		public Builder setSymmetricRtp(boolean value) {
			_symmetricRtp = value;
			return this;
		}

		/**
		 * @see StreamerOptions#sequenceCheck()
		 */
		public Builder setSequenceCheck(boolean sequenceCheck) {
			_sequenceCheck = sequenceCheck;
			return this;
		}

		/**
		 * @see StreamerOptions#silencePadding()
		 */
		public Builder setSilencePadding(boolean silencePadding) {
			_silencePadding = silencePadding;
			return this;
		}

		/**
		 * @see StreamerOptions#randomEarlyDrop()
		 */
		public Builder setRandomEarlyDrop(int red) {
			_red = red;
			return this;
		}

		/**
		 * @see StreamerOptions#syncAdjust()
		 */
		public Builder setSyncAdjust(long syncAdjust) {
			_syncAdjust = syncAdjust;
			return this;
		}

		/**
		 * @see StreamerOptions#ssrcCheck()
		 */
		public Builder setSsrcCheck(boolean ssrcCheck) {
			_ssrcCheck = ssrcCheck;
			return this;
		}
	
		/**
		 * Creates the {@link StreamerOptions} to use.
		 */
		public StreamerOptions build() {
			return new StreamerOptions() {
				@Override
				public boolean rtp() {
					return _rtp;
				}
	
				@Override
				public Codec additionalCodec() {
					return _codec;
				}
	
				@Override
				public boolean symmetricRtp() {
					return _symmetricRtp;
				}

				@Override
				public boolean sequenceCheck() {
					return _sequenceCheck;
				}

				@Override
				public boolean silencePadding() {
					return _silencePadding;
				}

				@Override
				public int randomEarlyDrop() {
					return _red;
				}

				@Override
				public long syncAdjust() {
					return _syncAdjust;
				}

				@Override
				public boolean ssrcCheck() {
					return _ssrcCheck;
				}
			};
		}
	}

}
