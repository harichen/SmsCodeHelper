package me.drakeet.inmessage.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import me.drakeet.inmessage.R;
import me.drakeet.inmessage.api.OnItemClickListener;
import me.drakeet.inmessage.model.Message;
import me.drakeet.inmessage.utils.SmsUtils;
import me.drakeet.inmessage.utils.TaskUtils;
import me.drakeet.inmessage.utils.VersionUtils;

/**
 * Created by shengkun on 15/6/5.
 */
public class MainMessageAdapter extends RecyclerView.Adapter<MainMessageAdapter.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_DATE,
        ITEM_TYPE_MESSAGE
    }

    private List<Message> mList;
    private Context mContext;
    private SmsUtils mSmsUtils;
    private Boolean mShowResult = false;

    private OnItemClickListener listener;


    public MainMessageAdapter(Context context, List<Message> messageList) {
        mList = messageList;
        mSmsUtils = new SmsUtils(context);
        mContext = context;
    }

    @Override
    public MainMessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        if (viewType == ITEM_TYPE.ITEM_TYPE_DATE.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_separation, parent, false);
            viewHolder = new ViewHolder(v);
            viewHolder.dateTv = (TextView) v.findViewById(R.id.date_message_tv);
            viewHolder.shadow = v.findViewById(R.id.ig_shadow);
        }
        if (viewType == ITEM_TYPE.ITEM_TYPE_MESSAGE.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            viewHolder = new ViewHolder(v);
            viewHolder.authorTv = (TextView) v.findViewById(R.id.author_message_tv);
            viewHolder.contentTv = (TextView) v.findViewById(R.id.content_message_tv);
            viewHolder.avatarTv = (TextView) v.findViewById(R.id.avatar_tv);
            viewHolder.dateTv = (TextView) v.findViewById(R.id.message_date_tv);
            viewHolder.itemLl = (LinearLayout) v.findViewById(R.id.item_message);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MainMessageAdapter.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_MESSAGE.ordinal()) {
            holder.authorTv.setText(mList.get(position).getSender());
            if(mShowResult && mList.get(position).getResultContent() != null) {
                holder.contentTv.setText(mList.get(position).getResultContent());
            }
            else {
                holder.contentTv.setText(mList.get(position).getContent());
            }
            if (mList.get(position).getReceiveDate() != null) {
                holder.dateTv.setText(mList.get(position).getReceiveDate());
            }
            holder.authorTv.setText(mList.get(position).getSender());
            if(mList.get(position).getCompanyName() != null) {
                String showCompanyName = mList.get(position).getCompanyName();
                if(showCompanyName.length() == 4) {
                    String fourCharsName = "";
                    for(int u = 0; u < showCompanyName.length();u ++) {
                        if(u == 2) {
                            fourCharsName += "\n";
                        }
                        fourCharsName += showCompanyName.charAt(u);
                    }
                    showCompanyName = fourCharsName;
                }
                holder.avatarTv.setText(showCompanyName);
            }
            else {
                holder.avatarTv.setText("?");
            }
            if (listener != null) {
                holder.itemLl.setOnClickListener(
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                listener.onItemClick(v, position);
                            }
                        }
                );
            }
        }
        else {
            holder.dateTv.setText(mList.get(position).getReceiveDate());
            if(needShowShadow(position)) {
                holder.shadow.setVisibility(View.VISIBLE);
            } else {
                holder.shadow.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    private void getAvatar(final String phoneNumber, final ImageView imageView, final TextView textView, final Message message) {
        TaskUtils.executeAsyncTask(
                new AsyncTask<Object, Object, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Object... params) {
                        Bitmap bitmap = mSmsUtils.get_people_image(phoneNumber);
                        return bitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap o) {
                        super.onPostExecute(o);
                        if (o != null) {
                            imageView.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.GONE);
                            imageView.setImageBitmap(o);
                        } else {
                            textView.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.GONE);
                        }
                    }
                }
        );
    }

    private void getName(final String phoneNumber, final TextView textView, final Message message) {
        TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object... params) {
                return mSmsUtils.getContactNameFromPhoneBook(phoneNumber);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null) {
                    textView.setText(s);
                    message.setAuthor(s);
                }
            }
        });
    }

    private Boolean needShowShadow(int position) {
        if (VersionUtils.IS_MORE_THAN_LOLLIPOP) {
            return false;
        }
        if(position == 0) {
            return false;
        }
        else if(mList.get(position - 1).getIsMessage()) {
            return true;
        }
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getIsMessage() ? ITEM_TYPE.ITEM_TYPE_MESSAGE.ordinal() : ITEM_TYPE.ITEM_TYPE_DATE.ordinal();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        TextView authorTv;
        TextView contentTv;
        TextView avatarTv;
        TextView dateTv;
        View shadow;
        LinearLayout itemLl;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public void setShowResult(Boolean showResult) {
        this.mShowResult = showResult;
    }


    @Override
    public void onViewRecycled(MainMessageAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
    }
}
