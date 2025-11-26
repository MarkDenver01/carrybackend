package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;


@Repository
public interface JpaProductRepository extends JpaRepository<Product, Long> {

    // 🔹 Get latest products (used in homepage)
    List<Product> findTop20ByOrderByProductIdDesc();

    // 🔹 Find product by primary productId
    Product findByProductId(Long productId);

    // 🔹 Filter by status ("Available", "Out of Stock", etc.)
    List<Product> findByProductStatus(String productStatus);

    // 🔹 Search matching product name — used in AI keyword searching
    List<Product> findByProductNameContainingIgnoreCase(String keyword);

    // 🔹 Search by category name
    List<Product> findByCategory_CategoryNameContainingIgnoreCase(String keyword);

    // 🔹 Get products by category object
    List<Product> findByCategory(ProductCategory category);


    // ==========================
    //   🔥 AI-SPECIFIC QUERIES
    // ==========================

    // 🔹 Show only AVAILABLE products
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.productStatus = 'Available'
        ORDER BY p.productInDate DESC
    """)
    List<Product> findAllActiveProducts(); // “Active” = available products


    // 🔹 Used when GPT returns a sorted list of product IDs
    @Query("""
        SELECT p 
        FROM Product p
        WHERE p.productId IN :ids
    """)
    List<Product> findByProductIds(List<Long> ids);


    // 🔹 Search for products using GPT keyword list
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.productStatus = 'Available'
          AND (
              LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(p.productDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    List<Product> searchByKeyword(String keyword);


    // 🔹 Best seller fallback — uses "stocks" or "sold" column if you have one
    @Query("""
        SELECT p 
        FROM Product p
        WHERE p.productStatus = 'Available'
        ORDER BY p.stocks ASC
    """)
    List<Product> findBestSellers();


    // 🔹 Newest products fallback
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.productStatus = 'Available'
        ORDER BY p.productInDate DESC
    """)
    List<Product> findNewestProducts();
    // ❌ OUT OF STOCK (0 stocks or status 'Out of Stock')
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stocks <= 0 OR p.productStatus = 'OUT_OF_STOCK'")
    long countOutOfStock();

    // ⏳ EXPIRING SOON (expiry range)
    @Query("SELECT COUNT(p) FROM Product p " +
            "WHERE p.productStatus = 'Available' " +
            "AND p.expiryDate IS NOT NULL " +
            "AND p.expiryDate BETWEEN :now AND :limit")
    long countExpiringSoon(
            @Param("now") LocalDateTime now,
            @Param("limit") LocalDateTime limit
    );

    // 📉 LOW STOCK
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stocks <= :threshold")
    long countLowStock(@Param("threshold") int threshold);
}

