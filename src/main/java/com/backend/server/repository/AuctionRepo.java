package com.backend.server.repository;

import com.backend.server.entity.Auction;
import com.backend.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepo extends JpaRepository<Auction,Long> {
    public List<Auction> findAuctionsBySellerId(Long seller_id);
}
