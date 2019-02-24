package com.myapplication.home.albums_fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.myapplication.R;
import com.myapplication.YasmaApplication;
import com.myapplication.data.db.YasmaDatabase;
import com.myapplication.data.model.Album;
import com.myapplication.home.HomeActivity;
import com.myapplication.network.FailureResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AlbumFragment extends Fragment implements AlbumView, AlbumAdapter.AlbumItemSelectedListener {

    @BindView(R.id.rv_albums)
    RecyclerView rvAlbums;
    @BindView(R.id.pb_progress)
    ProgressBar pbProgress;
    Unbinder unbinder;

    private AlbumFragmentInteractionListener listener;
    private AlbumPresenter albumPresenter;
    private AlbumAdapter albumAdapter;
    private List<Album> albumList = new ArrayList<>();

    public static AlbumFragment getInstance() {
        AlbumFragment postFragment = new AlbumFragment();
        return postFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeActivity) {
            listener = (AlbumFragmentInteractionListener) context;
        } else
            throw new IllegalStateException("Home must implement AlbumFragmentInteractionListener in order to communicate with activity");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        unbinder = ButterKnife.bind(this, view);
        setUpRecyclerView();
        albumPresenter = new AlbumPresenter(this);
        albumPresenter.fetchPosts();
        return view;
    }

    private void setUpRecyclerView() {
        albumAdapter = new AlbumAdapter(albumList, this);
        rvAlbums.setNestedScrollingEnabled(true);
        rvAlbums.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAlbums.setHasFixedSize(true);
        rvAlbums.setAdapter(albumAdapter);
    }

    @Override
    public void onAlbumsFetched(List<Album> albumList) {
        AlbumFragment.this.albumList.clear();
        AlbumFragment.this.albumList.addAll(albumList);
        albumAdapter.notifyDataSetChanged();
    }

    @Override
    public void showNoNetworkError() {
        hideLoadingBar();
        YasmaDatabase.getInstance(YasmaApplication.getInstance()).albumDao().getAlbums()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new MaybeObserver<List<Album>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<Album> albumList) {
                        AlbumFragment.this.albumList.clear();
                        AlbumFragment.this.albumList.addAll(albumList);
                        albumAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void showSnackbarLong(String message) {
        hideLoadingBar();
        listener.showSnackBar(message);
    }

    @Override
    public void showSpecificError(FailureResponse failureResponse) {
        hideLoadingBar();
        listener.showSnackBar(failureResponse.getMsg());
    }

    @Override
    public void showLoadingBar() {
        pbProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingBar() {
        pbProgress.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onAlbumSelected(Album album) {

    }

    public interface AlbumFragmentInteractionListener {
        void showSnackBar(String message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        albumPresenter.destroy();
    }
}
