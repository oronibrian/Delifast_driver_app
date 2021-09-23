package com.zap.zapdriver.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zap.zapdriver.Model.PackageModel;
import com.zap.zapdriver.R;

import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.BookingViewHolder> {

    /*variable declaration*/
    private final Context mCtx;
    private final List<PackageModel> mBookList;

    /*constructor*/
    public PackageAdapter(Context aCtx, List<PackageModel> aBookList) {
        /* initialize parameter*/
        this.mCtx = aCtx;
        this.mBookList = aBookList;

    }

    /*  inflate layout */
    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookingViewHolder(LayoutInflater.from(mCtx).inflate(R.layout.item_booking, null));
    }

    /*bind viewholder*/
    @Override
    public void onBindViewHolder(@NonNull final BookingViewHolder holder1, int position) {
        final PackageModel mBookingModel = mBookList.get(position);

        holder1.mTvDestination.setText(mBookingModel.getDate());
        holder1.mTvDuration.setText(mBookingModel.getTitle());
        holder1.mTvStartTime.setText(mBookingModel.getPickup_name());
        holder1.mTvSeatNo.setText(mBookingModel.getDropoff_name());
        holder1.mTvEndTime.setText(mBookingModel.getDistance() + " km");
        holder1.mTvPNRNo.setText("KES: " + mBookingModel.getCost());
        holder1.mTvTicketNo.setText(mBookingModel.getDistance() + " km");

        if (mBookingModel.getStatus().equalsIgnoreCase("delivered")) {

            holder1.statusimage.setImageResource(R.drawable.ic_completed);

        } else if (mBookingModel.getStatus().equalsIgnoreCase("canceled")) {

            holder1.statusimage.setImageResource(R.drawable.ic_canceled);

        } else {

            holder1.statusimage.setImageResource(R.drawable.riderremove);

        }

        holder1.mTvConfirm.setText(mBookingModel.getStatus());

        holder1.mRlContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
//                ((BaseActivity)mCtx).showView(holder1.mRlShowMore);
//                ((BaseActivity)mCtx).hideView(holder1.mIvShowMore);
//                ((BaseActivity)mCtx).hideView(holder1.mTvConfirm);


            }
        });
        holder1.mRlShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ((BaseActivity)mCtx).showView(holder1.mIvShowMore);
//                ((BaseActivity)mCtx).hideView(holder1.mRlShowMore);
//                ((BaseActivity)mCtx).fadeOutIn(holder1.mIvShowMore);
//                ((BaseActivity)mCtx).showView(holder1.mTvConfirm);
//                ((BaseActivity)mCtx).fadeOutIn(holder1.mTvConfirm);
            }
        });
    }

    /*item count*/
    @Override
    public int getItemCount() {
        return mBookList.size();
    }

    /*view holder*/
    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTvDestination;
        private final TextView mTvDuration;
        private final TextView mTvStartTime;
        private final TextView mTvEndTime;
        private final TextView mTvTicketNo;
        private final TextView mTvPNRNo;
        private TextView tvDestination;
        private final TextView mTvSeatNo;
        private final TextView mTvConfirm;
        private final RelativeLayout mRlShowMore;
        private final RelativeLayout mRlContent;
        private ImageView statusimage;

        BookingViewHolder(View itemView) {
            super(itemView);
            mTvDestination = itemView.findViewById(R.id.tvDestination);
            mTvDuration = itemView.findViewById(R.id.tvDuration);
            mTvStartTime = itemView.findViewById(R.id.tvStartTime);
            mTvEndTime = itemView.findViewById(R.id.tvEndTime);
            mTvTicketNo = itemView.findViewById(R.id.tvTicketNo);
            mTvPNRNo = itemView.findViewById(R.id.tvPNRNo);
            mRlShowMore = itemView.findViewById(R.id.rlShowMore);
            mRlContent = itemView.findViewById(R.id.rlContent);
            mTvSeatNo = itemView.findViewById(R.id.tvSeatNo);
            mTvConfirm = itemView.findViewById(R.id.tvConfirmed);

            statusimage = itemView.findViewById(R.id.statusimage);


        }
    }
}
