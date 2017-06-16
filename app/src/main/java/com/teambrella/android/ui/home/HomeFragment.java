package com.teambrella.android.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teambrella.android.R;

import java.util.ArrayList;


/**
 * Home Fragment
 */
public class HomeFragment extends Fragment {

    private RecyclerView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        mListView = (RecyclerView) view.findViewById(R.id.list);
        mListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mListView.setAdapter(new RecyclerView.Adapter() {

            private ArrayList<Integer> mList = new ArrayList<>();

            {
                for (int i = 1000; i < 1050; i++) {
                    mList.add(i);
                }
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new MyViewHolder(new TextView(getContext()));
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                ((TextView) holder.itemView).setText("" + mList.get(position));

                if (position == 3) {
                    mListView.postDelayed(() -> {
                        ArrayList<Integer> list = new ArrayList<>();
                        for (int i = mList.get(0) - 20; i < mList.get(0); i++) {
                            list.add(i);
                        }
                        mList.addAll(0, list);
                        notifyItemRangeInserted(0, 20);
                    }, 1000);
                }
            }

            @Override
            public int getItemCount() {
                return mList.size();
            }
        });
        return view;
    }


    public static final class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
