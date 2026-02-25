// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "SolanaKotlin",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "SolanaKotlin",
            targets: ["SolanaKotlin"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "SolanaKotlin",
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.3.5/SolanaKotlin.zip",
            checksum: "b493bb5f7bf426fffc94508052087763f322908788666b5741ce225aebdde80e"
        ),
    ]
)
